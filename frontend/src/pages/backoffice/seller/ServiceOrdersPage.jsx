import { useEffect, useMemo, useRef, useState } from 'react';
import { Wrench, UserPlus, CheckCircle2, XCircle, ClipboardCheck } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { SectionHeader, Panel, Summary, SearchBox, Modal, Field } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import ReceiptModal, { ReceiptButton } from '@/components/shared/ReceiptModal';
import { money } from '@/lib/format';

import {
    fetchServiceOrders,
    fetchServiceEmployees,
    fetchAssignmentsByOrder,
    createAssignment,
    confirmServiceOrder,
    completeServiceOrder,
    cancelServiceOrder,
} from '@/lib/sellerApi';

function normalizeComparable(value) {
    return String(value ?? '')
        .trim()
        .toUpperCase()
        .replaceAll('_', ' ')
        .replace(/\s+/g, ' ');
}

function prettyStatus(value) {
    const s = String(value ?? '').toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function extractSelectValue(valOrEvent) {
    if (valOrEvent == null) return '';
    if (typeof valOrEvent === 'object') {
        if ('target' in valOrEvent && valOrEvent.target) return String(valOrEvent.target.value ?? '');
        if ('value' in valOrEvent) return String(valOrEvent.value ?? '');
    }
    return String(valOrEvent);
}

function toArray(data) {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.content)) return data.content;
    if (Array.isArray(data?.data)) return data.data;
    return [];
}

function fmtDateTime(value) {
    if (!value) return 'Sin definir';
    const d = new Date(value);
    return Number.isNaN(d.getTime())
        ? String(value)
        : d.toLocaleString('es-PE', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

// Reintenta si la petición falla (por ejemplo, si la sesión aún no está lista al entrar).
async function safeFetch(fn, retries = 2) {
    let lastError = null;
    for (let attempt = 0; attempt <= retries; attempt++) {
        try {
            return { ok: true, data: await fn() };
        } catch (err) {
            lastError = err;
            if (attempt < retries) await sleep(500 * (attempt + 1));
        }
    }
    return { ok: false, data: null, error: lastError };
}

// Nombre del vendedor que atendió la orden (según los campos que mande el backend).
function pickSellerName(o) {
    const candidates = [
        o?.nombreVendedor,
        o?.vendedorNombre,
        o?.nombre_vendedor,
        o?.vendedor?.nombreUsuario,
        o?.vendedor?.nombre,
        typeof o?.vendedor === 'string' ? o.vendedor : '',
    ];
    const found = candidates.find(c => typeof c === 'string' && c.trim());
    return found ? found.trim().replace(/\s+/g, ' ') : '';
}

function pickSellerId(o) {
    return o?.idVendedor ?? o?.idUsuarioVendedor ?? o?.vendedor?.idUsuario ?? '';
}

// ¿La orden fue atendida por el usuario logueado?
function isMine(o, user) {
    const myIds = [user?.idUsuario, user?.id, user?.userId]
        .filter(v => v != null && v !== '')
        .map(String);
    if (myIds.length && o.idVendedor !== '' && myIds.includes(String(o.idVendedor))) return true;

    const myNames = [user?.nombreUsuario, user?.nombre]
        .map(normalizeComparable)
        .filter(Boolean);
    const sellerName = normalizeComparable(o.vendedorNombre);
    return Boolean(sellerName) && myNames.includes(sellerName);
}

function hasIdentity(user) {
    return [user?.idUsuario, user?.id, user?.userId, user?.nombreUsuario, user?.nombre].some(
        v => v != null && String(v).trim() !== ''
    );
}

function normalizeServiceOrder(o) {
    const rawDate = o.fechaProgramadaServicio || o.fechaOrdenServicio || '';
    const fecha = typeof rawDate === 'string' ? rawDate.slice(0, 10) : '';
    const vendedorNombre = pickSellerName(o);
    return {
        id: o.idOrdenServicio,
        servicio: o.nombreServicio || 'Servicio',
        cliente: o.nombreCliente || 'Cliente',
        estado: String(o.estadoServicio || 'PENDIENTE').trim().toUpperCase(),
        fecha,
        fechaOrden: o.fechaOrdenServicio || '',
        fechaProgramada: o.fechaProgramadaServicio || '',
        metodoPago: o.metodoPagoServicio || '-',
        total: Number(o.montoTotalServicio ?? 0),
        comprobante: o.numeroComprobanteServicio
            ? `${o.serieComprobanteServicio}-${o.numeroComprobanteServicio}`
            : '',
        incidencia: o.incidenciaServicio || '',
        requeridos: Number(o.cantidadPersonalRequerida ?? 1),
        asignados: Number(o.cantidadPersonalAsignada ?? 0),
        idVendedor: pickSellerId(o),
        vendedorNombre,
        vendedor: vendedorNombre || 'Por asignar',
        auxiliar: o.nombreAuxiliar || 'Por asignar',
        telefonoCliente: o.telefonoCliente || '',
        direccion: o.direccionServicio || '',
        detalle: o.detalleSolicitudServicio || '',
    };
}

export default function ServiceOrdersPage() {
    const { user } = useAuth();
    const defaultResponsible =
        user?.nombreUsuario || user?.nombre || user?.correoUsuario || user?.email || '';

    const [orders, setOrders] = useState([]);
    const [employees, setEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [feedback, setFeedback] = useState('');

    const [search, setSearch] = useState('');
    const [status, setStatus] = useState('');

    const [selected, setSelected] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [loadingAssignments, setLoadingAssignments] = useState(false);

    const [assignForm, setAssignForm] = useState({
        idEmpleado: '',
        nombreResponsableAsignacion: defaultResponsible,
        descripcionAsignacion: '',
    });
    const [incidencia, setIncidencia] = useState('');
    const [busyAction, setBusyAction] = useState(false);
    const [receipt, setReceipt] = useState(null);
    const [touchedIds, setTouchedIds] = useState(() => new Set());
    const requestRef = useRef(0);

    async function loadInitial() {
        // Evita que una respuesta vieja pise a una más nueva.
        const reqId = ++requestRef.current;
        const isStale = () => reqId !== requestRef.current;

        setLoading(true);
        setError('');
        const [ordersRes, employeesRes] = await Promise.all([
            safeFetch(fetchServiceOrders),
            safeFetch(fetchServiceEmployees),
        ]);
        if (isStale()) return;

        if (ordersRes.ok) {
            setOrders(toArray(ordersRes.data).map(normalizeServiceOrder));
        } else {
            setError(
                ordersRes.error?.message ||
                    'No se pudieron cargar las órdenes de servicio. Pulsa "Reintentar".'
            );
        }
        setEmployees(toArray(employeesRes.data));
        setLoading(false);
    }

    useEffect(() => {
        const requests = requestRef;
        const timer = setTimeout(() => { loadInitial(); }, 0);
        return () => { clearTimeout(timer); requests.current++; };
    }, []);

    async function openOrderModal(order) {
        setSelected(order);
        setIncidencia(order.incidencia || '');
        setFeedback('');
        setError('');
        setAssignForm({
            idEmpleado: '',
            nombreResponsableAsignacion: defaultResponsible,
            descripcionAsignacion: '',
        });
        setLoadingAssignments(true);
        try {
            const list = await fetchAssignmentsByOrder(order.id);
            setAssignments(Array.isArray(list) ? list : []);
        } catch {
            setAssignments([]);
        } finally {
            setLoadingAssignments(false);
        }
    }

    // Las órdenes que gestiono en esta sesión se mantienen visibles aunque el backend aún no me registre como vendedor.
    function markTouched(orderId) {
        setTouchedIds(prev => new Set(prev).add(orderId));
    }

    async function refreshSingleOrder(orderId) {
        const all = await fetchServiceOrders();
        const normalized = toArray(all).map(normalizeServiceOrder);
        setOrders(normalized);
        const updated = normalized.find(item => item.id === orderId) || null;
        setSelected(updated);
        return updated;
    }

    async function handleAssignEmployee(e) {
        e.preventDefault();
        if (!selected || !assignForm.idEmpleado) return;
        setBusyAction(true);
        setError('');
        setFeedback('');
        try {
            await createAssignment({
                idOrdenServicio: selected.id,
                idEmpleado: assignForm.idEmpleado,
                nombreResponsableAsignacion: assignForm.nombreResponsableAsignacion,
                descripcionAsignacion: assignForm.descripcionAsignacion,
            });
            const [updatedAssignments] = await Promise.all([
                fetchAssignmentsByOrder(selected.id).catch(() => []),
                refreshSingleOrder(selected.id),
            ]);
            setAssignments(Array.isArray(updatedAssignments) ? updatedAssignments : []);
            setAssignForm(prev => ({ ...prev, idEmpleado: '', descripcionAsignacion: '' }));
            setFeedback('Personal asignado correctamente.');
        } catch (err) {
            setError(err.message || 'No se pudo asignar el personal.');
        } finally {
            setBusyAction(false);
        }
    }

    async function handleConfirm() {
        if (!selected) return;
        setBusyAction(true);
        setError('');
        setFeedback('');
        try {
            await confirmServiceOrder(selected.id, incidencia);
            markTouched(selected.id);
            await refreshSingleOrder(selected.id);
            setFeedback('Orden de servicio confirmada.');
        } catch (err) {
            setError(err.message || 'No se pudo confirmar la orden (verifica el personal requerido).');
        } finally {
            setBusyAction(false);
        }
    }

    async function handleComplete() {
        if (!selected) return;
        setBusyAction(true);
        setError('');
        setFeedback('');
        try {
            await completeServiceOrder(selected.id, incidencia);
            markTouched(selected.id);
            await refreshSingleOrder(selected.id);
            setFeedback('Servicio marcado como completado.');
        } catch (err) {
            setError(err.message || 'No se pudo completar la orden.');
        } finally {
            setBusyAction(false);
        }
    }

    async function handleCancel() {
        if (!selected) return;
        setBusyAction(true);
        setError('');
        setFeedback('');
        try {
            await cancelServiceOrder(selected.id, incidencia);
            markTouched(selected.id);
            await refreshSingleOrder(selected.id);
            setFeedback('Orden de servicio cancelada.');
        } catch (err) {
            setError(err.message || 'No se pudo cancelar la orden.');
        } finally {
            setBusyAction(false);
        }
    }

    // Pendientes: las ve cualquier vendedor (cualquiera puede tomarlas).
    // Resto de estados: solo las que yo atendí.
    const visibleOrders = useMemo(() => {
        if (!hasIdentity(user)) return orders;
        return orders.filter(
            o =>
                normalizeComparable(o.estado) === 'PENDIENTE' ||
                touchedIds.has(o.id) ||
                isMine(o, user)
        );
    }, [orders, user, touchedIds]);

    const statusOptions = useMemo(() => {
        const base = [
            { value: 'PENDIENTE', label: 'Pendiente' },
            { value: 'CONFIRMADO', label: 'Confirmado' },
            { value: 'EN_PROCESO', label: 'En proceso' },
            { value: 'COMPLETADO', label: 'Completado' },
            { value: 'CANCELADO', label: 'Cancelado' },
        ];
        const known = new Map(base.map(o => [normalizeComparable(o.value), o]));
        visibleOrders.forEach(o => {
            const key = normalizeComparable(o.estado);
            if (key && !known.has(key)) known.set(key, { value: key, label: prettyStatus(key) });
        });
        return Array.from(known.values());
    }, [visibleOrders]);

    const rows = useMemo(() => {
        const term = search.trim().toLowerCase();
        const selectedStatus = normalizeComparable(status);
        return visibleOrders.filter(o => {
            const text = `${o.id} ${o.cliente} ${o.servicio}`.toLowerCase();
            const matchesText = !term || text.includes(term);
            const matchesStatus = !selectedStatus || normalizeComparable(o.estado) === selectedStatus;
            return matchesText && matchesStatus;
        });
    }, [visibleOrders, search, status]);

    const canConfirm = selected ? selected.asignados >= selected.requeridos : false;
    const selectedState = selected ? normalizeComparable(selected.estado) : '';
    // PENDIENTE -> se asigna personal y se confirma. CONFIRMADO -> se marca como completado. Ambas se pueden cancelar.
    const isPending = selectedState === 'PENDIENTE';
    const canComplete = ['CONFIRMADO', 'EN PROCESO'].includes(selectedState);

    return (
        <>
            <SectionHeader
                icon={Wrench}
                title="Solicitudes de servicio"
                description="Revisa solicitudes de servicio, asigna personal requerido y confirma o cancela órdenes."
            />

            <Summary
                items={[
                    ['Órdenes', visibleOrders.length],
                    ['Pendientes', visibleOrders.filter(o => normalizeComparable(o.estado) === 'PENDIENTE').length],
                    ['Confirmadas', visibleOrders.filter(o => normalizeComparable(o.estado) === 'CONFIRMADO').length],
                    ['Canceladas', visibleOrders.filter(o => normalizeComparable(o.estado) === 'CANCELADO').length],
                ]}
            />

            {error && !selected && (
                <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                    <span>{error}</span>
                    <Button variant="outline" size="sm" onClick={loadInitial} disabled={loading}>
                        {loading ? 'Cargando...' : 'Reintentar'}
                    </Button>
                </div>
            )}

            <Panel>
                <div className="mb-6 flex flex-wrap items-end gap-3">
                    <SearchBox value={search} onChange={setSearch} placeholder="Buscar servicio, cliente o ID..." />
                    <Select
                        className="w-full sm:w-52"
                        aria-label="Estado"
                        placeholder="Todos los estados"
                        value={status}
                        onChange={e => setStatus(extractSelectValue(e))}
                        options={statusOptions}
                    />
                </div>

<div className="w-full overflow-x-auto">
    <DataTable
        className="w-full"
        emptyText={loading ? 'Cargando órdenes de servicio...' : 'No hay órdenes de servicio para mostrar.'}
        rows={rows}
        columns={[
            {
                key: 'servicio',
                label: 'Servicio',
                className: 'px-2',
                render: o => <span className="text-xs sm:text-sm">{o.servicio}</span>,
            },
            {
                key: 'cliente',
                label: 'Cliente',
                className: 'px-2',
                render: o => <span className="text-xs sm:text-sm">{o.cliente}</span>,
            },
            {
                key: 'fecha',
                label: 'Fecha',
                className: 'w-24 whitespace-nowrap px-1.5',
                render: o => <span className="whitespace-nowrap text-xs sm:text-sm">{o.fecha}</span>,
            },
            {
                key: 'personal',
                label: 'Personal',
                className: 'w-12 whitespace-nowrap px-1 text-center',
                render: o => (
                    <span
                        className={`text-xs ${
                            o.asignados >= o.requeridos
                                ? 'font-semibold text-emerald-700'
                                : 'font-semibold text-amber-700'
                        }`}
                    >
                        {o.asignados}/{o.requeridos}
                    </span>
                ),
            },
            {
                key: 'total',
                label: 'Total',
                className: 'w-20 whitespace-nowrap px-1',
                render: o => <span className="whitespace-nowrap text-xs">{money(o.total)}</span>,
            },
            {
                key: 'estado',
                label: 'Estado',
                className: 'w-20 whitespace-nowrap px-1',
                render: o => {
                    const st = normalizeComparable(o.estado);
                    const variant =
                        st === 'CONFIRMADO' || st === 'COMPLETADO'
                            ? 'success'
                            : st === 'CANCELADO'
                                ? 'danger'
                                : 'warning';
                    return (
                        <Badge variant={variant} className="px-1.5 py-0.5 text-[10px]">
                            {String(o.estado).replaceAll('_', ' ')}
                        </Badge>
                    );
                },
            },
            {
                key: 'accion',
                label: 'Acción',
                className: 'w-28 whitespace-nowrap pl-1 pr-2',
                render: o => (
                    <div className="flex items-center justify-center gap-1 [&_button]:!px-2 [&_button]:!py-1 [&_button]:!text-[11px]">
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={() => openOrderModal(o)}
                        >
                            Gestionar
                        </Button>
                        <ReceiptButton
                            disponible={Boolean(o.comprobante)}
                            onClick={() => setReceipt(o)}
                        />
                    </div>
                ),
            },
        ]}
    />
</div>
            </Panel>

            {selected && (
                <Modal title={`Orden de servicio #${selected.id}`} onClose={() => setSelected(null)}>
                    <div className="space-y-5 text-sm">
                        <dl className="grid grid-cols-2 gap-4 rounded-xl bg-gray-50 p-4">
                            {[
                                ['Servicio', selected.servicio],
                                ['Cliente', selected.cliente],
                                ['Teléfono del cliente', selected.telefonoCliente || '-'],
                                ['Fecha programada', fmtDateTime(selected.fechaProgramada)],
                                ['Dirección', selected.direccion || '-'],
                                ['Atendido por', selected.vendedor],
                                ['Estado', String(selected.estado).replaceAll('_', ' ')],
                                ['Método de pago', selected.metodoPago],
                                ['Personal requerido', `${selected.asignados} de ${selected.requeridos}`],
                                ['Monto total', money(selected.total)],
                                ['Comprobante', selected.comprobante || 'Aún no emitido'],
                            ].map(([k, v]) => (
                                <div key={k}>
                                    <dt className="text-xs text-gray-500">{k}</dt>
                                    <dd className="mt-0.5 font-medium text-gray-800">{v}</dd>
                                </div>
                            ))}
                        </dl>

                        {selected.detalle && (
                            <div className="rounded-xl bg-blue-50 p-4 text-xs text-gray-700">
                                <p className="mb-1 font-semibold text-brand">Detalle de la solicitud</p>
                                <p className="whitespace-pre-wrap">{selected.detalle}</p>
                            </div>
                        )}

                        {selected.comprobante && (
                            <ReceiptButton variant="primary" disponible onClick={() => setReceipt(selected)} />
                        )}

                        {error && (
                            <div className="rounded-xl border border-red-200 bg-red-50 px-3.5 py-2.5 text-xs text-red-700">
                                {error}
                            </div>
                        )}
                        {feedback && (
                            <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-3.5 py-2.5 text-xs text-emerald-700">
                                {feedback}
                            </div>
                        )}

                        <div>
                            <h3 className="mb-2 font-semibold text-gray-800">Personal asignado</h3>
                            {loadingAssignments ? (
                                <p className="text-xs text-gray-500">Cargando asignaciones...</p>
                            ) : assignments.length === 0 ? (
                                <p className="text-xs text-gray-500">Aún no hay empleados asignados a esta orden.</p>
                            ) : (
                                <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200/80">
                                    {assignments.map(a => (
                                        <li key={a.idAsignacion} className="px-3.5 py-2.5 text-xs">
                                            <p className="font-semibold text-gray-800">{a.nombreCompletoEmpleado}</p>
                                            <p className="text-gray-600">{a.descripcionAsignacion}</p>
                                            <p className="mt-0.5 text-[11px] text-gray-400">
                                                Responsable: {a.nombreResponsableAsignacion}
                                            </p>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>

                        {isPending && (
                            <form onSubmit={handleAssignEmployee} className="space-y-3 border-t border-gray-100 pt-4">
                                <h3 className="font-semibold text-gray-800">Asignar empleado de servicio</h3>
                                <Field label="Empleado">
                                        <Select
                                            required
                                            placeholder="Seleccionar empleado"
                                            value={assignForm.idEmpleado}
                                            onChange={e => setAssignForm({ ...assignForm, idEmpleado: extractSelectValue(e) })}
                                            options={employees.map(emp => {
                                                const id = emp.idEmpleadoServicio ?? emp.id;
                                                const fullName =
                                                    `${emp.nombreEmpleadoServicio ?? ''} ${emp.apellidoEmpleadoServicio ?? ''}`.trim() ||
                                                    emp.nombre ||
                                                    `Empleado #${id}`;
                                                return {
                                                    value: String(id),
                                                    label: fullName,
                                                };
                                            })}
                                        />
                                </Field>

                                <Field label="Responsable de asignación">
                                    <input
                                        required
                                        className="bo-input"
                                        value={assignForm.nombreResponsableAsignacion}
                                        onChange={e =>
                                            setAssignForm({
                                                ...assignForm,
                                                nombreResponsableAsignacion: e.target.value,
                                            })
                                        }
                                        placeholder="Nombre del responsable"
                                    />
                                </Field>

                                <Field label="Descripción de la asignación">
                                    <input
                                        required
                                        className="bo-input"
                                        value={assignForm.descripcionAsignacion}
                                        onChange={e =>
                                            setAssignForm({
                                                ...assignForm,
                                                descripcionAsignacion: e.target.value,
                                            })
                                        }
                                        placeholder="Ej. Fumigación y desinfección de local"
                                    />
                                </Field>

                                <Button type="submit" disabled={busyAction} className="w-full justify-center">
                                    <UserPlus size={16} className="mr-2" />
                                    Asignar personal
                                </Button>
                            </form>
                        )}

                        <div className="space-y-3 border-t border-gray-100 pt-4">
                            <Field label="Incidencia u observación (opcional)">
                                <input
                                    className="bo-input"
                                    value={incidencia}
                                    onChange={e => setIncidencia(e.target.value)}
                                    placeholder="Nota al confirmar, completar o cancelar..."
                                />
                            </Field>

                            {!canConfirm && isPending && (
                                <p className="text-xs text-amber-700">
                                    Falta asignar personal ({selected.asignados}/{selected.requeridos}) para poder confirmar la orden.
                                </p>
                            )}

                            {(isPending || canComplete) && (
                                <div className="flex flex-wrap justify-end gap-2 pt-1">
                                    <Button
                                        type="button"
                                        variant="outline"
                                        disabled={busyAction}
                                        onClick={handleCancel}
                                    >
                                        <XCircle size={16} className="mr-1.5 text-red-600" />
                                        Cancelar orden
                                    </Button>
                                    {isPending && (
                                        <Button
                                            type="button"
                                            disabled={busyAction || !canConfirm}
                                            onClick={handleConfirm}
                                        >
                                            <CheckCircle2 size={16} className="mr-1.5" />
                                            Confirmar orden
                                        </Button>
                                    )}
                                    {canComplete && (
                                        <Button
                                            type="button"
                                            disabled={busyAction}
                                            onClick={handleComplete}
                                        >
                                            <ClipboardCheck size={16} className="mr-1.5" />
                                            Marcar como completado
                                        </Button>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>
                </Modal>
            )}

            {receipt && (
                <ReceiptModal
                    kind="servicio"
                    id={receipt.id}
                    numero={receipt.comprobante}
                    onClose={() => setReceipt(null)}
                />
            )}
        </>
    );
}