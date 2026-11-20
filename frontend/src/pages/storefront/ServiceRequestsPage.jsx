import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ClipboardList, ShoppingBag, Wrench, Truck, CheckCircle2, Clock, AlertTriangle, RefreshCw } from 'lucide-react';
import { fetchClientOrders, fetchClientServiceOrders } from '@/lib/clienteApi';
import { money } from '@/lib/format';
import ReceiptModal, { ReceiptButton } from '@/components/shared/ReceiptModal';
import { Panel, Field, SectionHeader } from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';

const BADGES = {
    PENDIENTE: ['Pendiente de confirmación', 'bg-amber-50 text-amber-700 border-amber-200'],
    CONFIRMADO: ['Confirmado', 'bg-blue-50 text-blue-700 border-blue-200'],
    EN_RUTA: ['En ruta', 'bg-indigo-50 text-indigo-700 border-indigo-200'],
    PENDIENTE_ENTREGA: ['Pendiente de entrega', 'bg-indigo-50 text-indigo-700 border-indigo-200'],
    ENTREGADO: ['Entregado', 'bg-emerald-50 text-emerald-700 border-emerald-200'],
    COMPLETADO: ['Completado', 'bg-emerald-50 text-emerald-700 border-emerald-200'],
    CANCELADO: ['Cancelado', 'bg-red-50 text-red-700 border-red-200'],
};

const fmtDate = v => {
    if (!v) return 'Por confirmar';
    const d = new Date(v);
    return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
};

const estDate = (base, fallback) => {
    if (fallback) return fmtDate(fallback);
    if (!base) return 'Por confirmar';
    const d = new Date(base);
    if (Number.isNaN(d.getTime())) return 'Por confirmar';
    d.setDate(d.getDate() + 2);
    return d.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
};

const comp = (s, n) => [s, n].map(x => String(x ?? '').trim()).filter(Boolean).join('-');

function DeliveryStatus({ estado, refDate, endDate }) {
    const st = String(estado || '').toUpperCase();
    if (st === 'CANCELADO') return <p className="mt-4 flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-xs font-medium text-red-700"><AlertTriangle size={15} />Este pedido fue cancelado.</p>;
    if (['ENTREGADO', 'COMPLETADO'].includes(st)) return <p className="mt-4 flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-xs font-medium text-emerald-800"><CheckCircle2 size={15} className="text-emerald-600" />Entregado el <strong>{fmtDate(endDate || refDate)}</strong></p>;
    return <div className="mt-4 flex items-center justify-between gap-2 rounded-xl border border-gray-200/80 bg-gray-50 px-4 py-3 text-xs text-gray-700"><span className="flex items-center gap-2"><Clock size={15} className="text-brand" />Fecha de entrega estimada: <strong>{estDate(refDate, endDate)}</strong></span><span className="text-[11px] text-gray-500">Sujeta a variaciones</span></div>;
}

function EmptyState({ icon: Icon, title, desc, to, cta }) {
    return <Panel className="py-14 text-center"><Icon size={40} className="mx-auto mb-3 text-brand" /><p className="text-base font-semibold text-gray-900">{title}</p><p className="mb-5 mt-1 text-sm text-gray-500">{desc}</p><Button as={Link} to={to}>{cta}</Button></Panel>;
}

function ManagementView({ quote, setQuote }) {
    return <>
        <SectionHeader icon={ClipboardList} title="Solicitudes de servicios" description="Revisión de necesidades y preparación de cotizaciones." />
        <Panel><p role="status" className="rounded-xl bg-blue-50 p-4 text-sm leading-6 text-brand">El backend de esta rama todavía no dispone del flujo de solicitudes y cotizaciones. Esta vista no crea ni modifica registros.</p><div className="py-12 text-center text-gray-500">No hay solicitudes disponibles para consultar.</div></Panel>
        <Panel className="mt-6">
            <h2 className="mb-5 text-lg font-semibold">Formulario de cotización</h2>
            <div className="grid gap-5 sm:grid-cols-2">
                <Field label="Precio cotizado (S/)"><input className="bo-input" type="number" min="0.01" step="0.01" value={quote.precio} onChange={e => setQuote({ ...quote, precio: e.target.value })} /></Field>
                <Field label="Fecha y hora disponibles"><input className="bo-input" type="datetime-local" value={quote.programada} onChange={e => setQuote({ ...quote, programada: e.target.value })} /></Field>
            </div>
            <div className="mt-5"><Field label="Detalle de la propuesta"><textarea className="bo-input" rows={3} value={quote.observaciones} onChange={e => setQuote({ ...quote, observaciones: e.target.value })} /></Field></div>
            <Button className="mt-5" disabled>Enviar cotización</Button>
        </Panel>
    </>;
}

export default function ServiceRequestsPage({ management = false }) {
    const [quote, setQuote] = useState({ precio: '', programada: '', observaciones: '' });
    const [tab, setTab] = useState('pedidos');
    const [orders, setOrders] = useState([]);
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [receipt, setReceipt] = useState(null);

    async function load() {
        setLoading(true);
        setError('');
        try {
            const [oRes, sRes] = await Promise.allSettled([fetchClientOrders(), fetchClientServiceOrders()]);
            if (oRes.status === 'fulfilled') setOrders(Array.isArray(oRes.value) ? oRes.value : []);
            if (sRes.status === 'fulfilled') setServices(Array.isArray(sRes.value) ? sRes.value : []);
            if (oRes.status === 'rejected' && sRes.status === 'rejected') setError(oRes.reason?.message || 'No se pudo cargar tu historial.');
        } finally { setLoading(false); }
    }

    useEffect(() => { if (!management) load(); }, [management]);
    if (management) return <ManagementView quote={quote} setQuote={setQuote} />;

    const badgeOf = st => BADGES[String(st || 'PENDIENTE').toUpperCase()] || [String(st).replaceAll('_', ' ').toLowerCase(), 'bg-gray-100 text-gray-700 border-gray-200'];

    return (
        <div className="mx-auto max-w-[1100px] px-4 py-10 md:px-6">
            <div className="flex flex-wrap items-start justify-between gap-4">
                <SectionHeader icon={ClipboardList} title="Mis solicitudes y pedidos" description="Revisa el estado de tus compras, servicios solicitados y consulta tu boleta." />
                <Button type="button" variant="secondary" onClick={load} disabled={loading} className="gap-2"><RefreshCw size={15} className={loading ? 'animate-spin' : ''} />Actualizar</Button>
            </div>

            <div className="mb-6 flex gap-2 border-b border-gray-200">
                {[['pedidos', 'Mis pedidos', ShoppingBag, orders.length], ['servicios', 'Mis servicios', Wrench, services.length]].map(([id, label, Icon, total]) => (
                    <button key={id} type="button" onClick={() => setTab(id)} className={`flex items-center gap-2 border-b-2 px-4 py-3 text-sm font-semibold transition ${tab === id ? 'border-brand text-brand' : 'border-transparent text-gray-500 hover:text-gray-800'}`}>
                        <Icon size={17} />{label}<span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-700">{total}</span>
                    </button>
                ))}
            </div>

            {error && <p role="alert" className="mb-6 rounded-xl bg-red-50 p-4 text-sm text-red-700">{error}</p>}

            {loading ? <Panel className="py-14 text-center text-sm text-gray-500">Cargando tu historial...</Panel> : tab === 'pedidos' ? (
                !orders.length ? <EmptyState icon={ShoppingBag} title="Aún no tienes pedidos registrados" desc="Explora nuestro catálogo de productos y realiza tu compra." to="/productos" cta="Explorar productos" /> : (
                    <div className="grid gap-5">
                        {orders.map((o, idx) => {
                            const [bLabel, bCls] = badgeOf(o.estadoPedido);
                            const comprobante = comp(o.serieComprobantePedido, o.numeroComprobantePedido);
                            return (
                                <Panel key={o.idPedido ?? idx}>
                                    <div className="flex flex-wrap items-start justify-between gap-4 border-b border-gray-100 pb-4">
                                        <div>
                                            <div className="flex flex-wrap items-center gap-2.5"><h2 className="text-lg font-semibold text-gray-900">Pedido {idx + 1}</h2><span className={`rounded-full border px-3 py-0.5 text-xs font-semibold ${bCls}`}>{bLabel}</span></div>
                                            <p className="mt-1 text-xs text-gray-500">Compra: <strong>{fmtDate(o.fechaPedido)}</strong> · Pago: <strong>{o.metodoPagoPedido}</strong>{comprobante && <> · Boleta: <strong>{comprobante}</strong></>}</p>
                                        </div>
                                        <div className="text-right"><p className="text-xs text-gray-500">Total</p><p className="text-xl font-bold text-brand">{money(o.montoTotalPedido)}</p></div>
                                    </div>
                                    <DeliveryStatus estado={o.estadoPedido} refDate={o.fechaSalidaPedido || o.fechaPedido} endDate={o.fechaEntregaPedido} />
                                    <div className="mt-4 divide-y divide-gray-100 rounded-xl border border-gray-100 px-4">
                                        {(o.detalles || []).map((d, i) => <div key={d.idDetallePedido ?? i} className="flex justify-between py-2.5 text-sm"><span className="text-gray-800"><strong>{d.cantidadProducto}x</strong> {d.nombreProducto}</span><span className="font-medium text-gray-700">{money(d.subtotal)}</span></div>)}
                                    </div>
                                    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 pt-2 text-xs text-gray-600">
                                        <span className="flex items-center gap-1.5"><Truck size={15} className="text-brand" />Repartidor: <strong>{o.nombreDelivery || 'Por asignar'}</strong></span>
                                        <ReceiptButton disponible={Boolean(comprobante)} onClick={() => setReceipt({ kind: 'pedido', id: o.idPedido, comprobante })} />
                                    </div>
                                    {o.incidentePedido && <p className="mt-3 rounded-lg bg-amber-50 p-3 text-xs text-amber-800"><strong>Nota de entrega:</strong> {o.incidentePedido}</p>}
                                </Panel>
                            );
                        })}
                    </div>
                )
            ) : !services.length ? <EmptyState icon={Wrench} title="No tienes servicios solicitados aún" desc="Solicita una cotización para desinfección o saneamiento de tu espacio." to="/servicios" cta="Explorar servicios" /> : (
                <div className="grid gap-5">
                    {services.map((s, idx) => {
                        const id = s.idOrdenServicio ?? s.idOrden ?? s.id;
                        const estado = s.estadoOrdenServicio ?? s.estadoOrden ?? s.estadoServicio ?? s.estado ?? 'PENDIENTE';
                        const [bLabel, bCls] = badgeOf(estado);
                        const comprobante = comp(s.serieComprobanteOrden ?? s.serieComprobanteServicio ?? s.serieComprobante, s.numeroComprobanteOrden ?? s.numeroComprobanteServicio ?? s.numeroComprobante);
                        const nombreSrv = s.nombreServicio || s.servicio?.nombreServicio || 'Servicio solicitado';
                        const fBase = s.fechaProgramadaOrden || s.fechaProgramadaServicio || s.fechaServicio || s.fechaSolicitud;
                        const fEnd = s.fechaEntregaOrden || s.fechaEntregaServicio || s.fechaEntrega;
                        const monto = Number(s.montoTotalOrden ?? s.montoTotalServicio ?? s.precioCotizado ?? s.montoTotal ?? 0);
                        return (
                            <Panel key={id ?? idx}>
                                <div className="flex flex-wrap items-start justify-between gap-4 border-b border-gray-100 pb-4">
                                    <div>
                                        <div className="flex flex-wrap items-center gap-2.5"><h2 className="text-lg font-semibold text-gray-900">Servicio {idx + 1}</h2><span className={`rounded-full border px-3 py-0.5 text-xs font-semibold ${bCls}`}>{bLabel}</span></div>
                                        <p className="mt-1 text-xs text-gray-500"><strong>{nombreSrv}</strong>{fBase && <> · Programado: <strong>{fmtDate(fBase)}</strong></>}{comprobante && <> · Boleta: <strong>{comprobante}</strong></>}</p>
                                    </div>
                                    {monto > 0 && <div className="text-right"><p className="text-xs text-gray-500">Total</p><p className="text-xl font-bold text-brand">{money(monto)}</p></div>}
                                </div>
                                <DeliveryStatus estado={estado} refDate={fBase} endDate={fEnd} />
                                {(s.direccionServicio || s.observacionesOrden || s.descripcionOrden) && (
                                    <div className="mt-3 space-y-1 text-xs text-gray-600">
                                        {s.direccionServicio && <p><strong>Dirección:</strong> {s.direccionServicio}</p>}
                                        {(s.observacionesOrden || s.descripcionOrden) && <p><strong>Detalle:</strong> {s.observacionesOrden || s.descripcionOrden}</p>}
                                    </div>
                                )}
                                <div className="mt-4 flex justify-end"><ReceiptButton disponible={Boolean(comprobante && id)} onClick={() => setReceipt({ kind: 'servicio', id, comprobante })} /></div>
                            </Panel>
                        );
                    })}
                </div>
            )}

            {receipt && <ReceiptModal kind={receipt.kind} id={receipt.id} numero={receipt.comprobante} onClose={() => setReceipt(null)} />}
        </div>
    );
}