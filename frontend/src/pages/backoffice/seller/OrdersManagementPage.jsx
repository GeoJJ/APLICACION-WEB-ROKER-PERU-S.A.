import { useEffect, useMemo, useState } from 'react';
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

function normalizeProductOrder(o, kind = 'pedido') {
    const rawDate = o.fechaPedido || '';
    const fecha = typeof rawDate === 'string' ? rawDate.slice(0, 10) : '';
    const comprobante = formatReceipt(o.serieComprobantePedido, o.numeroComprobantePedido);
    const vendedor =
        o.nombreVendedor ||
        o.vendedor?.nombreUsuario ||
        (o.idVendedor ? `Vendedor #${o.idVendedor}` : 'Sin asignar');

    return {
        uid: `${kind}-${o.idPedido}`,
        kind: 'pedido',
        id: o.idPedido,
        codigo: `#${o.idPedido}`,
        concepto: 'Pedido de productos',
        cliente: o.nombreCliente || 'Cliente',
        vendedor,
        direccion: o.direccionCliente || 'Sin dirección',
        estado: o.estadoPedido || 'PENDIENTE',
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
    const vendedor =
        o.nombreVendedor ||
        o.vendedor?.nombreUsuario ||
        (o.idVendedor ? `Vendedor #${o.idVendedor}` : 'Sin asignar');

    return {
        uid: `servicio-${o.idOrdenServicio}`,
        kind: 'servicio',
        id: o.idOrdenServicio,
        codigo: `SRV-${o.idOrdenServicio}`,
        concepto: o.nombreServicio || 'Servicio',
        cliente: o.nombreCliente || 'Cliente',
        vendedor,
        direccion: 'Servicio programado',
        estado: o.estadoServicio || 'CONFIRMADO',
        fecha,
        fechaSalidaPedido: '',
        fechaEntregaPedido: '',
        metodoPago: o.metodoPagoServicio || '-',
        total: Number(o.montoTotalServicio ?? 0),
        comprobante,
        idDelivery: '',
        nombreDelivery: vendedor,
        detalles: [],
    };
}

function badgeVariant(estado) {
    const st = String(estado || '').toUpperCase();
    if (['ENTREGADO', 'COMPLETADO'].includes(st)) return 'success';
    if (['CANCELADO'].includes(st)) return 'danger';
    if (['PENDIENTE'].includes(st)) return 'warning';
    return 'info';
}

function formatStatus(estado) {
    if (!estado) return '';
    const clean = String(estado).replaceAll('_', ' ').toLowerCase();
    return clean.charAt(0).toUpperCase() + clean.slice(1);
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

    async function loadInitial() {
        setLoading(true);
        setError('');
        try {
            if (sales) {
                const [allSalesRes, pendingRes, mySalesRes, servicesRes] = await Promise.all([
                    fetchAllSellerSales().catch(() => null),
                    fetchSellerOrders().catch(() => []),
                    fetchSellerSales().catch(() => []),
                    fetchServiceOrders().catch(() => []),
                ]);

                const map = new Map();
                const productSalesSource = Array.isArray(allSalesRes)
                    ? allSalesRes
                    : [
                        ...(Array.isArray(pendingRes) ? pendingRes : []),
                        ...(Array.isArray(mySalesRes) ? mySalesRes : []),
                    ];

                productSalesSource.forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });

                (Array.isArray(servicesRes) ? servicesRes : []).forEach(o => {
                    const norm = normalizeServiceSale(o);
                    map.set(norm.uid, norm);
                });

                setOrders(Array.from(map.values()).filter(r => Boolean(r.comprobante)));
                setDrivers([]);
            } else {
                const [pendingRes, myOrdersRes, driversRes] = await Promise.all([
                    fetchSellerOrders().catch(() => []),
                    fetchSellerSales().catch(() => []),
                    fetchDeliveryDrivers().catch(() => []),
                ]);

                const map = new Map();
                (Array.isArray(pendingRes) ? pendingRes : []).forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });
                (Array.isArray(myOrdersRes) ? myOrdersRes : []).forEach(o => {
                    const norm = normalizeProductOrder(o);
                    map.set(norm.uid, norm);
                });

                setOrders(Array.from(map.values()));
                setDrivers(Array.isArray(driversRes) ? driversRes : []);
            }
        } catch (err) {
            setError(err.message || 'No se pudieron cargar los registros.');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        setStatus('');
        setSellerFilter('');
        loadInitial();
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
            fetchSellerOrders().catch(() => []),
            fetchSellerSales().catch(() => []),
        ]);
        const map = new Map();
        (Array.isArray(pendingRes) ? pendingRes : []).forEach(o => {
            const norm = normalizeProductOrder(o);
            map.set(norm.uid, norm);
        });
        (Array.isArray(myOrdersRes) ? myOrdersRes : []).forEach(o => {
            const norm = normalizeProductOrder(o);
            map.set(norm.uid, norm);
        });
        const list = Array.from(map.values());
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
            const isPending = String(selected.estado).toUpperCase() === 'PENDIENTE';
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
        const unique = new Set(orders.map(o => o.vendedor).filter(Boolean));
        return Array.from(unique);
    }, [orders]);

    const rows = useMemo(() => {
        return orders.filter(o => {
            const matchesText = `${o.codigo} ${o.cliente} ${o.concepto} ${o.vendedor} ${o.comprobante}`
                .toLowerCase()
                .includes(search.toLowerCase());
            const matchesStatus = !status || String(o.estado).toUpperCase() === status;
            const matchesSeller = !sales || !sellerFilter || o.vendedor === sellerFilter;
            return matchesText && matchesStatus && matchesSeller;
        });
    }, [orders, search, status, sales, sellerFilter]);

    const summaryItems = sales
        ? [
            ['Ventas con comprobante', orders.length],
            ['Pedidos de productos', orders.filter(o => o.kind === 'pedido').length],
            ['Órdenes de servicio', orders.filter(o => o.kind === 'servicio').length],
            ['Monto total', money(rows.reduce((acc, o) => acc + o.total, 0))],
        ]
        : [
            ['Pedidos', orders.length],
            ['Pendientes', orders.filter(o => String(o.estado).toUpperCase() === 'PENDIENTE').length],
            ['Confirmados / En ruta', orders.filter(o => ['CONFIRMADO', 'EN_RUTA', 'EN_PROCESO'].includes(String(o.estado).toUpperCase())).length],
            ['Entregados / Cancelados', orders.filter(o => ['ENTREGADO', 'COMPLETADO', 'CANCELADO'].includes(String(o.estado).toUpperCase())).length],
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
                <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                    {error}
                </div>
            )}

            <Panel>
                <div className="mb-6 flex flex-wrap items-end gap-3">
                    <SearchBox
                        value={search}
                        onChange={setSearch}
                        placeholder={sales ? 'Buscar venta, cliente, vendedor o comprobante...' : 'Buscar pedido, cliente o ID...'}
                    />
                    <Select
                        className="w-full sm:max-w-52"
                        aria-label="Estado"
                        placeholder="Todos los estados"
                        value={status}
                        onChange={e => setStatus(e.target.value)}
                        options={[
                            { value: 'PENDIENTE', label: 'Pendiente' },
                            { value: 'CONFIRMADO', label: 'Confirmado' },
                            { value: 'EN_RUTA', label: 'En ruta' },
                            { value: 'ENTREGADO', label: 'Entregado' },
                            { value: 'COMPLETADO', label: 'Completado' },
                            { value: 'CANCELADO', label: 'Cancelado' },
                        ]}
                    />
                    {sales && (
                        <Select
                            className="w-full sm:max-w-52"
                            aria-label="Vendedor"
                            placeholder="Todos los vendedores"
                            value={sellerFilter}
                            onChange={e => setSellerFilter(e.target.value)}
                            options={sellerOptions.map(v => ({
                                value: v,
                                label: v,
                            }))}
                        />
                    )}
                </div>
<div className="w-full overflow-x-auto">
    <DataTable
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
                render: o => <span className="whitespace-nowrap text-xs sm:text-sm">{money(o.total)}</span>,
            },
            ...(sales
                ? [
                      {
                          key: 'comprobante',
                          label: 'Comprobante',
                          className: 'w-28 whitespace-nowrap px-1.5',
                          render: o => <span className="whitespace-nowrap text-xs">{o.comprobante || '-'}</span>,
                      },
                  ]
                : []),
            {
                key: 'estado',
                label: 'Estado',
                className: 'w-24 whitespace-nowrap px-1',
                render: o => (
                    <Badge variant={badgeVariant(o.estado)} className="px-1.5 py-0.5 text-[10px]">
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
                                    {String(selected.estado).toUpperCase() === 'PENDIENTE'
                                        ? 'Confirmar y asignar repartidor'
                                        : 'Editar asignación / fechas del pedido'}
                                </h3>

                                <Field label="Repartidor">
                                    <Select
                                        required
                                        placeholder="Seleccionar repartidor"
                                        value={assignForm.idDelivery}
                                        onChange={e => setAssignForm({ ...assignForm, idDelivery: e.target.value })}
                                        options={drivers.map(d => ({
                                            value: d.idUsuario,
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
                                        : String(selected.estado).toUpperCase() === 'PENDIENTE'
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