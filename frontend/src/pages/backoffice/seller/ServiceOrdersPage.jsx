import { useEffect, useMemo, useState } from 'react';
import { Wrench, UserPlus, CheckCircle2, XCircle } from 'lucide-react';
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
    cancelServiceOrder,
} from '@/lib/sellerApi';

function normalizeServiceOrder(o) {
    const rawDate = o.fechaProgramadaServicio || o.fechaOrdenServicio || '';
    const fecha = typeof rawDate === 'string' ? rawDate.slice(0, 10) : '';
    return {
        id: o.idOrdenServicio,
        servicio: o.nombreServicio || 'Servicio',
        cliente: o.nombreCliente || 'Cliente',
        estado: o.estadoServicio || 'PENDIENTE',
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
        vendedor: o.nombreVendedor || 'Por asignar',
        auxiliar: o.nombreAuxiliar || 'Por asignar',
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

    async function loadInitial() {
        setLoading(true);
        setError('');
        try {
            const [ordersRes, employeesRes] = await Promise.all([
                fetchServiceOrders(),
                fetchServiceEmployees().catch(() => []),
            ]);
            setOrders((Array.isArray(ordersRes) ? ordersRes : []).map(normalizeServiceOrder));
            setEmployees(Array.isArray(employeesRes) ? employeesRes : []);
        } catch (err) {
            setError(err.message || 'No se pudieron cargar las órdenes de servicio.');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadInitial();
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

    async function refreshSingleOrder(orderId) {
        const all = await fetchServiceOrders();
        const normalized = (Array.isArray(all) ? all : []).map(normalizeServiceOrder);
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
            await refreshSingleOrder(selected.id);
            setFeedback('Orden de servicio confirmada.');
        } catch (err) {
            setError(err.message || 'No se pudo confirmar la orden (verifica el personal requerido).');
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
            await refreshSingleOrder(selected.id);
            setFeedback('Orden de servicio cancelada.');
        } catch (err) {
            setError(err.message || 'No se pudo cancelar la orden.');
        } finally {
            setBusyAction(false);
        }
    }

    const rows = useMemo(() => {
        return orders.filter(o => {
            const matchesText = `${o.id} ${o.cliente} ${o.servicio}`.toLowerCase().includes(search.toLowerCase());
            const matchesStatus = !status || String(o.estado).toUpperCase() === status;
            return matchesText && matchesStatus;
        });
    }, [orders, search, status]);

    const canConfirm = selected ? selected.asignados >= selected.requeridos : false;
    const isClosed = selected
        ? ['CONFIRMADO', 'CANCELADO', 'COMPLETADO'].includes(String(selected.estado).toUpperCase())
        : false;

    return (
        <>
            <SectionHeader
                icon={Wrench}
                title="Solicitudes de servicio"
                description="Revisa solicitudes de servicio, asigna personal requerido y confirma o cancela órdenes."
            />

            <Summary
                items={[
                    ['Órdenes', orders.length],
                    ['Pendientes', orders.filter(o => String(o.estado).toUpperCase() === 'PENDIENTE').length],
                    ['Confirmadas', orders.filter(o => String(o.estado).toUpperCase() === 'CONFIRMADO').length],
                    ['Canceladas', orders.filter(o => String(o.estado).toUpperCase() === 'CANCELADO').length],
                ]}
            />

            {error && !selected && (
                <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                    {error}
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
                        onChange={e => setStatus(e.target.value)}
                        options={[
                            { value: 'PENDIENTE', label: 'Pendiente' },
                            { value: 'CONFIRMADO', label: 'Confirmado' },
                            { value: 'EN_PROCESO', label: 'En proceso' },
                            { value: 'COMPLETADO', label: 'Completado' },
                            { value: 'CANCELADO', label: 'Cancelado' },
                        ]}
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
                    const st = String(o.estado).toUpperCase();
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
                                ['Estado', selected.estado],
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

                        {!isClosed && (
                            <form onSubmit={handleAssignEmployee} className="space-y-3 border-t border-gray-100 pt-4">
                                <h3 className="font-semibold text-gray-800">Asignar empleado de servicio</h3>
                                <Field label="Empleado">
                                    <Field label="Empleado">
                                        <Select
                                            required
                                            placeholder="Seleccionar empleado"
                                            value={assignForm.idEmpleado}
                                            onChange={e => setAssignForm({ ...assignForm, idEmpleado: e.target.value })}
                                            options={employees.map(emp => {
                                                const id = emp.idEmpleadoServicio ?? emp.id;
                                                const fullName =
                                                    `${emp.nombreEmpleadoServicio ?? ''} ${emp.apellidoEmpleadoServicio ?? ''}`.trim() ||
                                                    emp.nombre ||
                                                    `Empleado #${id}`;
                                                return {
                                                    value: id,
                                                    label: fullName,
                                                };
                                            })}
                                        />
                                    </Field>
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
                                    placeholder="Detalle de incidencia al confirmar o cancelar..."
                                />
                            </Field>

                            {!canConfirm && !isClosed && (
                                <p className="text-xs text-amber-700">
                                    Falta asignar personal ({selected.asignados}/{selected.requeridos}) para poder confirmar la orden.
                                </p>
                            )}

                            {!isClosed && (
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
                                    <Button
                                        type="button"
                                        disabled={busyAction || !canConfirm}
                                        onClick={handleConfirm}
                                    >
                                        <CheckCircle2 size={16} className="mr-1.5" />
                                        Confirmar orden
                                    </Button>
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