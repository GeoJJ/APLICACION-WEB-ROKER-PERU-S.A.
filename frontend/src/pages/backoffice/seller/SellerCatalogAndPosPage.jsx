import { useEffect, useMemo, useState } from 'react';
import { Store, Package, Wrench, Plus, Trash2, CheckCircle2, RefreshCw } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { SectionHeader, Panel, Summary, SearchBox, Field } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import ReceiptModal, { ReceiptButton } from '@/components/shared/ReceiptModal';
import { money } from '@/lib/format';
import { fetchSellerProducts, searchSellerProducts, fetchSellerServices, createInStoreOrder } from '@/lib/sellerApi';

const TABS = [
    ['pos', 'Venta presencial', Store],
    ['products', 'Catálogo de productos', Package],
    ['services', 'Catálogo de servicios', Wrench],
];
const PAY_METHODS = ['EFECTIVO', 'YAPE', 'PLIN', 'TARJETA', 'TRANSFERENCIA'];
const isDni = v => /^\d{8}$/.test(v);
const stockText = (s, m) => (s === 0 ? 'Agotado' : s <= m ? 'Stock bajo' : 'Stock normal');

export default function SellerCatalogAndPosPage({ initialTab = 'pos' }) {
    const { user } = useAuth();
    const sellerName = user?.nombreUsuario || user?.nombre || 'Vendedor actual';

    const [tab, setTab] = useState(initialTab);
    const [products, setProducts] = useState([]);
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [msg, setMsg] = useState({ error: '', ok: '' });

    const [search, setSearch] = useState({ products: '', services: '' });
    const [selectedId, setSelectedId] = useState('');
    const [qty, setQty] = useState(1);
    const [cart, setCart] = useState([]);
    const [sale, setSale] = useState({ method: 'EFECTIVO', dni: '', busy: false, last: null, receipt: null });

    async function loadCatalog() {
        setLoading(true);
        setMsg({ error: '', ok: '' });
        try {
            const [p, s] = await Promise.all([fetchSellerProducts(), fetchSellerServices()]);
            setProducts(Array.isArray(p) ? p : []);
            setServices(Array.isArray(s) ? s : []);
        } catch (err) {
            setMsg({ error: err.message || 'No se pudo cargar el catálogo disponible.', ok: '' });
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => setTab(initialTab), [initialTab]);
    useEffect(() => { loadCatalog(); }, []);

    useEffect(() => {
        if (tab !== 'products') return;
        let active = true;
        const t = setTimeout(async () => {
            try {
                const q = search.products.trim();
                const res = await (q ? searchSellerProducts(q) : fetchSellerProducts());
                if (active) setProducts(Array.isArray(res) ? res : []);
            } catch {}
        }, 300);
        return () => { active = false; clearTimeout(t); };
    }, [search.products, tab]);

    function addItem(e) {
        e.preventDefault();
        setMsg({ error: '', ok: '' });
        const prod = products.find(p => String(p.idProducto) === String(selectedId));
        if (!prod) return;

        const nQty = Number(qty);
        const stock = Number(prod.stockProducto ?? 0);
        const cur = cart.find(i => i.idProducto === prod.idProducto)?.cantidadProducto ?? 0;
        if (nQty <= 0 || cur + nQty > stock) {
            return setMsg({ error: `Stock insuficiente para ${prod.nombreProducto}. Disponible: ${stock}.`, ok: '' });
        }

        setCart(prev => prev.some(i => i.idProducto === prod.idProducto)
            ? prev.map(i => i.idProducto === prod.idProducto ? { ...i, cantidadProducto: i.cantidadProducto + nQty } : i)
            : [...prev, { idProducto: prod.idProducto, nombreProducto: prod.nombreProducto, precioProducto: Number(prod.precioProducto ?? 0), cantidadProducto: nQty }]
        );
        setSelectedId('');
        setQty(1);
    }

    const totalCart = useMemo(() => cart.reduce((a, i) => a + i.precioProducto * i.cantidadProducto, 0), [cart]);

    async function confirmSale() {
        if (!cart.length) return;
        if (!isDni(sale.dni)) {
            return setMsg({ error: 'Ingresa un DNI válido de 8 dígitos para emitir la boleta.', ok: '' });
        }
        setSale(s => ({ ...s, busy: true, last: null }));
        setMsg({ error: '', ok: '' });
        try {
            const created = await createInStoreOrder({
                dniCliente: sale.dni,
                metodoPagoPedido: sale.method,
                detalles: cart.map(i => ({ idProducto: i.idProducto, cantidadProducto: i.cantidadProducto })),
            });
            const comp = created?.numeroComprobantePedido ? `${created.serieComprobantePedido}-${created.numeroComprobantePedido}` : '';
            setSale(s => ({ ...s, busy: false, dni: '', last: { id: created?.idPedido, comprobante: comp } }));
            setCart([]);
            setMsg({ error: '', ok: `Venta presencial registrada en estado COMPLETADO.${comp ? ` Comprobante: ${comp}.` : ''}` });
            await loadCatalog();
        } catch (err) {
            setSale(s => ({ ...s, busy: false }));
            setMsg({ error: err.message || 'No se pudo registrar la venta presencial.', ok: '' });
        }
    }

    const filteredServices = useMemo(() => {
        const q = search.services.toLowerCase().trim();
        return q ? services.filter(s => `${s.codigoServicio ?? ''} ${s.nombreServicio ?? ''} ${s.categoriaServicio ?? ''}`.toLowerCase().includes(q)) : services;
    }, [services, search.services]);

    const availableCount = useMemo(
        () => products.filter(p => Number(p.stockProducto ?? 0) > 0 && p.estadoProducto !== 'INACTIVO').length,
        [products]
    );

    const productCols = [
        { key: 'codigoProducto', label: 'Código', render: p => <span className="font-normal text-gray-700">{p.codigoProducto || '-'}</span> },
        { key: 'nombreProducto', label: 'Producto' },
        { key: 'categoriaProducto', label: 'Categoría', render: p => p.categoriaProducto || '-' },
        { key: 'precioProducto', label: 'Precio', render: p => money(p.precioProducto) },
        { key: 'estadoProducto', label: 'Estado', render: p => <Badge variant={p.estadoProducto !== 'INACTIVO' ? 'success' : 'warning'}>{p.estadoProducto !== 'INACTIVO' ? 'Activo' : 'Inactivo'}</Badge> },
        { key: 'stockProducto', label: 'Stock', render: p => { const s = Number(p.stockProducto ?? 0); return <>{s}<p className="text-xs text-gray-500">{stockText(s, Number(p.stockMinimo ?? 0))}</p></>; } },
    ];

    const serviceCols = [
        { key: 'codigoServicio', label: 'Código', render: s => <span className="font-normal text-gray-700">{s.codigoServicio || '-'}</span> },
        { key: 'categoriaServicio', label: 'Categoría', render: s => s.categoriaServicio || '-' },
        { key: 'nombreServicio', label: 'Servicio' },
        { key: 'precioServicio', label: 'Tarifa', render: s => money(s.precioServicio) },
        { key: 'cantidadPersonalServicio', label: 'Personal requerido', render: s => s.cantidadPersonalServicio ?? '-' },
    ];

    const activeProducts = products.filter(p => p.estadoProducto !== 'INACTIVO' && Number(p.stockProducto ?? 0) > 0);

    return <>
        <SectionHeader icon={Store} title="Mostrador y Catálogo"
            description="Registra ventas presenciales directas (sin repartidor, estado COMPLETADO) y consulta el catálogo disponible." />

        <Summary items={[['Productos disponibles', availableCount], ['Total productos', products.length], ['Servicios activos', services.length]]} />

        <div className="mb-5 flex flex-wrap gap-2">
            {TABS.map(([id, label, Icon]) => (
                <Button key={id} type="button" variant={tab === id ? 'primary' : 'outline'} size="sm" onClick={() => setTab(id)}>
                    <Icon size={15} className="mr-1.5" />{label}
                </Button>
            ))}
        </div>

        {msg.error && <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{msg.error}</div>}
        {msg.ok && <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
            <span>{msg.ok}</span>
            {sale.last?.id && <ReceiptButton variant="primary" disponible={Boolean(sale.last.comprobante)} onClick={() => setSale(s => ({ ...s, receipt: s.last }))} />}
        </div>}

        {tab === 'pos' && <Panel>
            <div className="space-y-6">
                <div className="grid gap-4 rounded-xl bg-gray-50 p-4 text-sm sm:grid-cols-3">
                    <div><span className="text-xs text-gray-500">Vendedor responsable</span><p className="mt-0.5 font-semibold text-gray-800">{sellerName}</p></div>
                    <div><span className="text-xs text-gray-500">Modalidad</span><p className="mt-0.5 font-semibold text-gray-800">Presencial en local (Sin repartidor)</p></div>
                    <div><span className="text-xs text-gray-500">Estado al registrar</span><div className="mt-0.5"><Badge variant="success">COMPLETADO</Badge></div></div>
                </div>

                <form onSubmit={addItem} className="grid grid-cols-1 gap-3 sm:grid-cols-12 sm:items-end">
                    <div className="sm:col-span-7">
                        <Field label="Producto">
                            <Select required placeholder="Seleccionar producto disponible..." value={selectedId} onChange={e => setSelectedId(e.target.value)}
                                options={activeProducts.map(p => ({ value: p.idProducto, label: `${p.nombreProducto} — ${money(p.precioProducto)} (Stock: ${p.stockProducto})` }))} />
                        </Field>
                    </div>
                    <div className="sm:col-span-2">
                        <Field label="Cantidad"><input type="number" min={1} required className="bo-input w-full" value={qty} onChange={e => setQty(e.target.value)} /></Field>
                    </div>
                    <div className="sm:col-span-3">
                        <Button type="submit" variant="outline" className="w-full justify-center"><Plus size={16} className="mr-1.5" />Agregar</Button>
                    </div>
                </form>

                {cart.length ? <div className="space-y-4">
                    <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200/80 text-sm">
                        {cart.map(item => (
                            <li key={item.idProducto} className="flex items-center justify-between gap-3 px-4 py-3">
                                <div><p className="font-medium text-gray-800">{item.nombreProducto}</p><p className="text-xs text-gray-500">{item.cantidadProducto} × {money(item.precioProducto)}</p></div>
                                <div className="flex items-center gap-3">
                                    <span className="font-semibold text-gray-900">{money(item.precioProducto * item.cantidadProducto)}</span>
                                    <button type="button" onClick={() => setCart(p => p.filter(x => x.idProducto !== item.idProducto))} className="rounded-lg p-1.5 text-red-600 hover:bg-red-50" title="Quitar producto"><Trash2 size={15} /></button>
                                </div>
                            </li>
                        ))}
                    </ul>
                    <div className="grid gap-4 sm:grid-cols-3 sm:items-end">
                        <Field label="DNI del cliente">
                            <input type="text" inputMode="numeric" maxLength={8} required placeholder="8 dígitos" className="bo-input w-full"
                                value={sale.dni} onChange={e => setSale(s => ({ ...s, dni: e.target.value.replace(/\D/g, '').slice(0, 8) }))} />
                        </Field>
                        <Field label="Método de pago">
                            <select className="bo-input" value={sale.method} onChange={e => setSale(s => ({ ...s, method: e.target.value }))}>
                                {PAY_METHODS.map(m => <option key={m} value={m}>{m}</option>)}
                            </select>
                        </Field>
                        <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                            <span className="text-sm text-gray-600">Total a cobrar</span>
                            <span className="text-xl font-bold text-gray-900">{money(totalCart)}</span>
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <Button type="button" disabled={sale.busy || !isDni(sale.dni)} onClick={confirmSale}>
                            <CheckCircle2 size={16} className="mr-2" />{sale.busy ? 'Registrando venta...' : 'Confirmar venta presencial'}
                        </Button>
                    </div>
                </div> : <p className="rounded-xl border border-dashed border-gray-200 py-8 text-center text-sm text-gray-500">Agrega productos arriba para registrar una venta presencial en mostrador.</p>}
            </div>
        </Panel>}

        {(tab === 'products' || tab === 'services') && <Panel>
            <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
                <SearchBox value={search[tab]} onChange={v => setSearch(s => ({ ...s, [tab]: v }))} placeholder={`Buscar ${tab === 'products' ? 'producto' : 'servicio'}...`} />
                <Button type="button" variant="outline" size="sm" onClick={loadCatalog}><RefreshCw size={14} className="mr-1.5" />Actualizar</Button>
            </div>
            <DataTable
                emptyText={loading ? `Cargando ${tab === 'products' ? 'productos' : 'servicios'}...` : `No hay ${tab === 'products' ? 'productos' : 'servicios'} disponibles.`}
                rows={tab === 'products' ? products : filteredServices}
                columns={tab === 'products' ? productCols : serviceCols}
            />
        </Panel>}

        {sale.receipt && <ReceiptModal kind="pedido" id={sale.receipt.id} numero={sale.receipt.comprobante} onClose={() => setSale(s => ({ ...s, receipt: null }))} />}
    </>;
}