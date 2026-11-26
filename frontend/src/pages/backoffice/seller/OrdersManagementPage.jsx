import { useEffect, useMemo, useRef, useState } from 'react';
import { ClipboardList, ShoppingCart, CheckCircle2 } from 'lucide-react';
import { SectionHeader, Panel, Summary, SearchBox, Modal, Field } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import ReceiptModal, { ReceiptButton } from '@/components/shared/ReceiptModal';
import { money } from '@/lib/format';
import {
    fetchSellerOrders,
    fetchSellerSales,
    fetchAllSellerSales,
    fetchServiceOrders,
    fetchDeliveryDrivers,
    confirmSellerOrder,
    updateSellerOrder,
} from '@/lib/sellerApi';

function formatReceipt(serie, numero) {
    const s = String(serie ?? '').trim();
    const n = String(numero ?? '').trim();
    if (s && n) return `${s}-${n}`;
    return n || s || '';
}

function normalizeComparable(value) {
    return String(value ?? '')
        .trim()
        .toUpperCase()
        .replaceAll('_', ' ')
        .replace(/\s+/g, ' ');
}

// Busca el nombre del vendedor en los distintos campos que puede mandar el backend.
function pickSellerName(o) {
    const candidates = [
        o?.nombreVendedor,
        o?.vendedorNombre,
        o?.nombre_vendedor,
        o?.nombreUsuarioVendedor,
        o?.vendedor?.nombreUsuario,
        o?.vendedor?.nombre,
        o?.vendedor?.nombreCompleto,
        o?.usuarioVendedor?.nombreUsuario,
        typeof o?.vendedor === 'string' ? o.vendedor : '',
    ];
    const found = candidates.find(c => typeof c === 'string' && c.trim());
    return found ? found.trim().replace(/\s+/g, ' ') : '';
}

function pickSellerId(o) {
    return o?.idVendedor ?? o?.idUsuarioVendedor ?? o?.vendedor?.idUsuario ?? '';
}

// Completa el nombre de los registros que solo traen el ID usando
// el nombre que otros registros del mismo vendedor sí traen.
function resolveSellerNames(list) {
    const byId = new Map();
    list.forEach(o => {
        if (o.vendedorNombre && o.idVendedor !== '') byId.set(String(o.idVendedor), o.vendedorNombre);
    });
    return list.map(o => {
        const nombre =
            o.vendedorNombre ||
            byId.get(String(o.idVendedor)) ||
            (o.idVendedor !== '' ? `Vendedor #${o.idVendedor}` : 'Sin asignar');
        return { ...o, vendedor: nombre };
    });
}

function toArray(data) {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.content)) return data.content;
    if (Array.isArray(data?.data)) return data.data;
    return [];
}

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

// Reintenta si la petición falla (por ejemplo, si el token aún no está listo al entrar).
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

function prettyStatus(value) {
    const s = String(value ?? '').toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
}

function normalizeProductOrder(o, kind = 'pedido') {
    const rawDate = o.fechaPedido || '';
    const fecha = typeof rawDate === 'string' ? rawDate.slice(0, 10) : '';
    const comprobante = formatReceipt(o.serieComprobantePedido, o.numeroComprobantePedido);
    const vendedorNombre = pickSellerName(o);

    return {
        uid: `${kind}-${o.idPedido}`,
        kind: 'pedido',
        id: o.idPedido,
        codigo: `#${o.idPedido}`,
        concepto: 'Pedido de productos',
        cliente: o.nombreCliente || (o.documentoCliente ? `DNI ${o.documentoCliente}` : 'Cliente'),
        idVendedor: pickSellerId(o),
        vendedorNombre,
        vendedor: vendedorNombre,
        direccion: o.direccionCliente || (o.documentoCliente ? 'Venta presencial' : 'Sin dirección'),
        estado: String(o.estadoPedido || 'PENDIENTE').trim().toUpperCase(),
        fecha,
        fechaSalidaPedido: o.fechaSalidaPedido || '',
        fechaEntregaPedido: o.fechaEntregaPedido || '',
        metodoPago: o.metodoPagoPedido || '-',
        total: Number(o.montoTotalPedido ?? 0),
        comprobante,
        idDelivery: o.idDelivery ?? '',
        nombreDelivery: o.nombreDelivery || 'Por asignar',
        detalles: Array.isArray(o.detalles) ? o.detalles : [],
    };
}

function normalizeServiceSale(o) {
    const rawDate = o.fechaProgramadaServicio || o.fechaOrdenServicio || '';
    const fecha = typeof rawDate === 'string' ? rawDate.slice(0, 10) : '';
    const comprobante = formatReceipt(o.serieComprobanteServicio, o.numeroComprobanteServicio);
    const vendedorNombre = pickSellerName(o);

    return {
        uid: `servicio-${o.idOrdenServicio}`,
        kind: 'servicio',
        id: o.idOrdenServicio,
        codigo: `SRV-${o.idOrdenServicio}`,
        concepto: o.nombreServicio || 'Servicio',
        cliente: o.nombreCliente || 'Cliente',
        idVendedor: pickSellerId(o),
        vendedorNombre,
        vendedor: vendedorNombre,
        direccion: 'Servicio programado',
        estado: String(o.estadoServicio || 'CONFIRMADO').trim().toUpperCase(),
        fecha,
        fechaSalidaPedido: '',
        fechaEntregaPedido: '',
        metodoPago: o.metodoPagoServicio || '-',
        total: Number(o.montoTotalServicio ?? 0),
        comprobante,
        idDelivery: '',
        nombreDelivery: vendedorNombre || 'Sin asignar',
        detalles: [],
    };
}

function badgeVariant(estado) {
    const st = normalizeComparable(estado);
    if (st === 'ENTREGADO' || st === 'COMPLETADO') return 'success';
    if (st === 'CANCELADO') return 'danger';
    if (st === 'PENDIENTE') return 'warning';
    return 'info';
}

function extractSelectValue(valOrEvent) {
    if (valOrEvent == null) return '';
    if (typeof valOrEvent === 'object') {
        if ('target' in valOrEvent && valOrEvent.target) {
            return String(valOrEvent.target.value ?? '');
        }
        if ('value' in valOrEvent) {
            return String(valOrEvent.value ?? '');
        }
    }
    return String(valOrEvent);
}

export default function OrdersManagementPage({ sales = false }) {
    const [orders, setOrders] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [feedback, setFeedback] = useState('');

    const [search, setSearch] = useState('');
    const [status, setStatus] = useState('');
    const [sellerFilter, setSellerFilter] = useState('');

    const [selected, setSelected] = useState(null);
    const [assignForm, setAssignForm] = useState({
        idDelivery: '',
        fechaSalidaPedido: '',
        fechaEntregaPedido: '',
    });
    const [busyAction, setBusyAction] = useState(false);
    const [receipt, setReceipt] = useState(null);
    const requestRef = useRef(0);

    async function loadInitial() {
        // Evita que una respuesta vieja pise a una más nueva (cambio de pantalla / doble render).
        const reqId = ++requestRef.current;
        const isStale = () => reqId !== requestRef.current;

        setLoading(true);
        setError('');
        try {
            if (sales) {
                const [allSalesRes, pendingRes, mySalesRes, servicesRes] = await Promise.all([
                    safeFetch(fetchAllSellerSales),
                    safeFetch(fetchSellerOrders),
                    safeFetch(fetchSellerSales),
                    safeFetch(fetchServiceOrders),
                ]);
                if (isStale()) return;

                const allSales = allSalesRes.ok ? toArray(allSalesRes.data) : null;
                // DIAGNÓSTICO: revisa la consola del navegador (F12) para ver qué campos manda el backend.
                console.debug('[Ventas] ejemplo pedido:', (allSales || toArray(mySalesRes.data))[0]);
                console.debug('[Ventas] ejemplo servicio:', toArray(servicesRes.data)[0]);
                console.debug('[Ventas] errores:', {
                    allSales: allSalesRes.error?.message,
                    pending: pendingRes.error?.message,
                    mine: mySalesRes.error?.message,
                    services: servicesRes.error?.message,
                });
                const productSalesSource = allSales
                    ? allSales
                    : [...toArray(pendingRes.data), ...toArray(mySalesRes.data)];

                const map = new Map();
                productSalesSource.forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });
                toArray(servicesRes.data).forEach(o => {
                    const norm = normalizeServiceSale(o);
                    map.set(norm.uid, norm);
                });

                const list = resolveSellerNames(
                    Array.from(map.values()).filter(r => Boolean(r.comprobante))
                );
                const productsFailed = !allSalesRes.ok && !pendingRes.ok && !mySalesRes.ok;
                if (productsFailed || !servicesRes.ok) {
                    setError(
                        'Algunos datos no se pudieron cargar. Pulsa "Reintentar" para volver a cargarlos.'
                    );
                }
                setOrders(list);
                setDrivers([]);
            } else {
                const [pendingRes, myOrdersRes, driversRes] = await Promise.all([
                    safeFetch(fetchSellerOrders),
                    safeFetch(fetchSellerSales),
                    safeFetch(fetchDeliveryDrivers),
                ]);
                if (isStale()) return;
                console.debug('[Pedidos] ejemplo:', toArray(pendingRes.data)[0] || toArray(myOrdersRes.data)[0]);

                const map = new Map();
                toArray(pendingRes.data).forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });
                toArray(myOrdersRes.data).forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });

                if (!pendingRes.ok || !myOrdersRes.ok) {
                    setError(
                        'Algunos pedidos no se pudieron cargar. Pulsa "Reintentar" para volver a cargarlos.'
                    );
                }
                setOrders(resolveSellerNames(Array.from(map.values())));
                setDrivers(toArray(driversRes.data));
            }
        } catch (err) {
            if (!isStale()) setError(err.message || 'No se pudieron cargar los registros.');
        } finally {
            if (!isStale()) setLoading(false);
        }
    }

    useEffect(() => {
        const requests = requestRef;
        const timer = setTimeout(() => {
            setSearch('');
            setStatus('');
            setSellerFilter('');
            setOrders([]);
            loadInitial();
        }, 0);
        return () => { clearTimeout(timer); requests.current++; };
        // loadInitial usa el modo sales y el contador para descartar respuestas antiguas.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sales]);

    function openOrderModal(order) {
        setSelected(order);
        setFeedback('');
        setError('');
        setAssignForm({
            idDelivery: order.idDelivery ? String(order.idDelivery) : '',
            fechaSalidaPedido: order.fechaSalidaPedido ? String(order.fechaSalidaPedido).slice(0, 16) : '',
            fechaEntregaPedido: order.fechaEntregaPedido ? String(order.fechaEntregaPedido).slice(0, 16) : '',
        });
    }

    async function refreshOrders() {
        const [pendingRes, myOrdersRes] = await Promise.all([
            safeFetch(fetchSellerOrders),
            safeFetch(fetchSellerSales),
        ]);
        const map = new Map();
        toArray(pendingRes.data).forEach(o => {
            const norm = normalizeProductOrder(o);
            map.set(norm.uid, norm);
        });
        toArray(myOrdersRes.data).forEach(o => {
            const norm = normalizeProductOrder(o);
            map.set(norm.uid, norm);
        });
        const list = resolveSellerNames(Array.from(map.values()));
        setOrders(list);
        return list;
    }

    async function handleSaveOrder(e) {
        e.preventDefault();
        if (!selected || selected.kind !== 'pedido' || !assignForm.idDelivery) return;
        setBusyAction(true);
        setError('');
        setFeedback('');
        try {
            const isPending = normalizeComparable(selected.estado) === 'PENDIENTE';
            if (isPending) {
                await confirmSellerOrder(selected.id, assignForm);
                const updatedList = await refreshOrders();
                setSelected(updatedList.find(item => item.uid === selected.uid) || null);
                setFeedback('Pedido confirmado y repartidor asignado correctamente.');
            } else {
                await updateSellerOrder(selected.id, assignForm);
                const updatedList = await refreshOrders();
                setSelected(updatedList.find(item => item.uid === selected.uid) || null);
                setFeedback('Pedido actualizado correctamente.');
            }
        } catch (err) {
            setError(err.message || 'No se pudo guardar el pedido.');
        } finally {
            setBusyAction(false);
        }
    }

    const sellerOptions = useMemo(() => {
        const unique = Array.from(
            new Set(orders.map(o => o.vendedor).filter(Boolean))
        ).sort((a, b) => a.localeCompare(b, 'es'));
        return unique.map(v => ({ value: v, label: v }));
    }, [orders]);

    // Estados base + cualquier otro estado que realmente venga en los datos
    // (así los servicios y pedidos con estados distintos también se pueden filtrar).
    const statusOptions = useMemo(() => {
        const base = [
            { value: 'PENDIENTE', label: 'Pendiente' },
            { value: 'CONFIRMADO', label: 'Confirmado' },
            { value: 'EN_RUTA', label: 'En ruta' },
            { value: 'ENTREGADO', label: 'Entregado' },
            ...(sales ? [{ value: 'COMPLETADO', label: 'Completado' }] : []),
            { value: 'CANCELADO', label: 'Cancelado' },
        ];
        const known = new Map(base.map(o => [normalizeComparable(o.value), o]));
        orders.forEach(o => {
            const key = normalizeComparable(o.estado);
            if (key && !known.has(key)) known.set(key, { value: key, label: prettyStatus(key) });
        });
        return Array.from(known.values());
    }, [sales, orders]);

    const rows = useMemo(() => {
        const term = search.trim().toLowerCase();
        const selectedStatus = normalizeComparable(status);
        const selectedSeller = normalizeComparable(sellerFilter);

        return orders.filter(o => {
            const hayTexto = [
                o.codigo,
                o.id,
                o.cliente,
                o.concepto,
                o.vendedor,
                o.comprobante,
            ]
                .filter(Boolean)
                .join(' ')
                .toLowerCase();

            const matchesText = !term || hayTexto.includes(term);
            const matchesStatus =
                !selectedStatus || normalizeComparable(o.estado) === selectedStatus;
            const matchesSeller =
                !sales ||
                !selectedSeller ||
                normalizeComparable(o.vendedor) === selectedSeller;

            return matchesText && matchesStatus && matchesSeller;
        });
    }, [orders, search, status, sales, sellerFilter]);

    const summaryItems = sales
        ? [
              ['Ventas con comprobante', rows.length],
              ['Pedidos de productos', rows.filter(o => o.kind === 'pedido').length],
              ['Órdenes de servicio', rows.filter(o => o.kind === 'servicio').length],
              ['Monto total', money(rows.reduce((acc, o) => acc + Number(o.total || 0), 0))],
          ]
        : [
              ['Pedidos', orders.length],
              ['Pendientes', orders.filter(o => normalizeComparable(o.estado) === 'PENDIENTE').length],
              [
                  'Confirmados / En ruta',
                  orders.filter(o =>
                      ['CONFIRMADO', 'EN RUTA', 'EN PROCESO'].includes(normalizeComparable(o.estado))
                  ).length,
              ],
              [
                  'Entregados / Cancelados',
                  orders.filter(o =>
                      ['ENTREGADO', 'CANCELADO'].includes(normalizeComparable(o.estado))
                  ).length,
              ],
          ];

    return (
        <>
            <SectionHeader
                icon={sales ? ShoppingCart : ClipboardList}
                title={sales ? 'Ventas' : 'Pedidos de productos'}
                description={
                    sales
                        ? 'Consulta las ventas de productos y servicios con comprobante emitido y filtra por vendedor.'
                        : 'Revisa pedidos de productos por estado, confirma pendientes o edita repartidor y fechas.'
                }
            />

            <Summary items={summaryItems} />

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
                    <SearchBox
                        value={search}
                        onChange={setSearch}
                        placeholder={
                            sales
                                ? 'Buscar venta, cliente, vendedor o comprobante...'
                                : 'Buscar pedido, cliente o ID...'
                        }
                    />
                    <Select
                        className="w-full sm:max-w-52"
                        aria-label="Estado"
                        placeholder="Todos los estados"
                        value={status}
                        onChange={e => setStatus(extractSelectValue(e))}
                        options={statusOptions}
                    />
                    {sales && (
                        <Select
                            className="w-full sm:max-w-52"
                            aria-label="Vendedor"
                            placeholder="Todos los vendedores"
                            value={sellerFilter}
                            onChange={e => setSellerFilter(extractSelectValue(e))}
                            options={sellerOptions}
                        />
                    )}
                </div>

                <div className="w-full overflow-x-auto">
                    <DataTable
                        key={`${status}|${sellerFilter}|${search}|${orders.length}`}
                        className="w-full"
                        emptyText={loading ? 'Cargando registros...' : 'No hay registros para mostrar.'}
                        rows={rows}
                        columns={[
                            ...(sales ? [{ key: 'concepto', label: 'Concepto', className: 'px-2' }] : []),
                            {
                                key: 'cliente',
                                label: 'Cliente',
                                className: 'px-2',
                                render: o => <span className="text-xs sm:text-sm">{o.cliente}</span>,
                            },
                            ...(sales
                                ? [
                                      {
                                          key: 'vendedor',
                                          label: 'Vendedor',
                                          className: 'px-2',
                                          render: o => <span className="text-xs sm:text-sm">{o.vendedor}</span>,
                                      },
                                  ]
                                : []),
                            {
                                key: 'fecha',
                                label: 'Fecha',
                                className: 'w-24 whitespace-nowrap px-1.5',
                                render: o => <span className="whitespace-nowrap text-xs sm:text-sm">{o.fecha}</span>,
                            },
                            {
                                key: 'total',
                                label: 'Total',
                                className: 'w-20 whitespace-nowrap px-1',
                                render: o => (
                                    <span className="whitespace-nowrap text-xs sm:text-sm">{money(o.total)}</span>
                                ),
                            },
                            ...(sales
                                ? [
                                      {
                                          key: 'comprobante',
                                          label: 'Comprobante',
                                          className: 'w-28 whitespace-nowrap px-1.5',
                                          render: o => (
                                              <span className="whitespace-nowrap text-xs">
                                                  {o.comprobante || '-'}
                                              </span>
                                          ),
                                      },
                                  ]
                                : []),
                            {
                                key: 'estado',
                                label: 'Estado',
                                className: 'w-24 whitespace-nowrap px-1',
                                render: o => (
                                    <Badge
                                        variant={badgeVariant(o.estado)}
                                        className="px-1.5 py-0.5 text-[10px]"
                                    >
                                        {String(o.estado).replaceAll('_', ' ')}
                                    </Badge>
                                ),
                            },
                            {
                                key: 'accion',
                                label: 'Acción',
                                className: 'w-28 whitespace-nowrap pl-1 pr-2',
                                render: o => (
                                    <div className="flex items-center justify-center gap-1 [&_button]:!px-2 [&_button]:!py-1 [&_button]:!text-[11px]">
                                        <Button variant="outline" size="sm" onClick={() => openOrderModal(o)}>
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
                <Modal title={`${selected.concepto} ${selected.codigo}`} onClose={() => setSelected(null)}>
                    <div className="space-y-5 text-sm">
                        <dl className="grid grid-cols-2 gap-4 rounded-xl bg-gray-50 p-4">
                            {[
                                ['Cliente', selected.cliente],
                                ['Vendedor', selected.vendedor],
                                ['Dirección', selected.direccion],
                                ['Estado', selected.estado],
                                ['Método de pago', selected.metodoPago],
                                ['Repartidor', selected.nombreDelivery],
                                ['Monto total', money(selected.total)],
                                ['Comprobante', selected.comprobante || 'Aún no emitido'],
                            ].map(([k, v]) => (
                                <div key={k}>
                                    <dt className="text-xs text-gray-500">{k}</dt>
                                    <dd className="mt-0.5 font-medium text-gray-800">{v}</dd>
                                </div>
                            ))}
                        </dl>

                        {selected.detalles.length > 0 && (
                            <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200/80 text-xs">
                                {selected.detalles.map(d => (
                                    <li key={d.idDetallePedido} className="flex justify-between gap-3 px-3.5 py-2.5">
                                        <span className="font-medium text-gray-800">
                                            {d.cantidadProducto} × {d.nombreProducto}
                                        </span>
                                        <span className="font-semibold text-gray-700">{money(d.subtotal)}</span>
                                    </li>
                                ))}
                            </ul>
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

                        {!sales && selected.kind === 'pedido' && (
                            <form onSubmit={handleSaveOrder} className="space-y-3 border-t border-gray-100 pt-4">
                                <h3 className="font-semibold text-gray-800">
                                    {normalizeComparable(selected.estado) === 'PENDIENTE'
                                        ? 'Confirmar y asignar repartidor'
                                        : 'Editar asignación / fechas del pedido'}
                                </h3>

                                <Field label="Repartidor">
                                    <Select
                                        required
                                        placeholder="Seleccionar repartidor"
                                        value={assignForm.idDelivery}
                                        onChange={e =>
                                            setAssignForm({
                                                ...assignForm,
                                                idDelivery: extractSelectValue(e),
                                            })
                                        }
                                        options={drivers.map(d => ({
                                            value: String(d.idUsuario),
                                            label: d.nombreUsuario,
                                        }))}
                                    />
                                </Field>

                                <div className="grid gap-3 sm:grid-cols-2">
                                    <Field label="Fecha de salida (opcional)">
                                        <input
                                            type="datetime-local"
                                            className="bo-input"
                                            value={assignForm.fechaSalidaPedido}
                                            onChange={e =>
                                                setAssignForm({ ...assignForm, fechaSalidaPedido: e.target.value })
                                            }
                                        />
                                    </Field>
                                    <Field label="Fecha de entrega (opcional)">
                                        <input
                                            type="datetime-local"
                                            className="bo-input"
                                            value={assignForm.fechaEntregaPedido}
                                            onChange={e =>
                                                setAssignForm({ ...assignForm, fechaEntregaPedido: e.target.value })
                                            }
                                        />
                                    </Field>
                                </div>

                                <Button type="submit" disabled={busyAction} className="w-full justify-center">
                                    <CheckCircle2 size={16} className="mr-2" />
                                    {busyAction
                                        ? 'Guardando...'
                                        : normalizeComparable(selected.estado) === 'PENDIENTE'
                                          ? 'Confirmar pedido'
                                          : 'Guardar cambios'}
                                </Button>
                            </form>
                        )}
                    </div>
                </Modal>
            )}

            {receipt && (
                <ReceiptModal
                    kind={receipt.kind === 'servicio' ? 'servicio' : 'pedido'}
                    id={receipt.id}
                    numero={receipt.comprobante}
                    onClose={() => setReceipt(null)}
                />
            )}
        </>
    );
}