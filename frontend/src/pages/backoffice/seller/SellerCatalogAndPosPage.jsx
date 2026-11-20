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
import {
    fetchSellerProducts,
    searchSellerProducts,
    fetchSellerServices,
    createInStoreOrder,
} from '@/lib/sellerApi';

export default function SellerCatalogAndPosPage({ initialTab = 'pos' }) {
    const { user } = useAuth();
    const sellerName = user?.nombreUsuario || user?.nombre || 'Vendedor actual';

    const [tab, setTab] = useState(initialTab); // 'pos' | 'products' | 'services'
    const [products, setProducts] = useState([]);
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [feedback, setFeedback] = useState('');

    // Búsqueda en catálogo
    const [productSearch, setProductSearch] = useState('');
    const [serviceSearch, setServiceSearch] = useState('');

    // Carrito de venta presencial
    const [selectedProductId, setSelectedProductId] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [cart, setCart] = useState([]);
    const [paymentMethod, setPaymentMethod] = useState('EFECTIVO');
    const [submittingSale, setSubmittingSale] = useState(false);
    const [lastSale, setLastSale] = useState(null);
    const [receipt, setReceipt] = useState(null);

    async function loadCatalog() {
        setLoading(true);
        setError('');
        try {
            const [prodRes, servRes] = await Promise.all([
                fetchSellerProducts(),
                fetchSellerServices(),
            ]);
            setProducts(Array.isArray(prodRes) ? prodRes : []);
            setServices(Array.isArray(servRes) ? servRes : []);
        } catch (err) {
            setError(err.message || 'No se pudo cargar el catálogo disponible.');
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        setTab(initialTab);
    }, [initialTab]);

    useEffect(() => {
        loadCatalog();
    }, []);

    useEffect(() => {
        if (tab !== 'products') return;
        let active = true;
        const timer = setTimeout(async () => {
            try {
                const res = productSearch.trim()
                    ? await searchSellerProducts(productSearch)
                    : await fetchSellerProducts();
                if (active) setProducts(Array.isArray(res) ? res : []);
            } catch {
                // Mantiene lista actual
            }
        }, 300);
        return () => {
            active = false;
            clearTimeout(timer);
        };
    }, [productSearch, tab]);

    function handleAddItem(e) {
        e.preventDefault();
        setError('');
        const prod = products.find(p => String(p.idProducto) === String(selectedProductId));
        if (!prod) return;

        const qty = Number(quantity);
        const stock = Number(prod.stockProducto ?? 0);
        const existing = cart.find(item => item.idProducto === prod.idProducto);
        const currentQty = existing ? existing.cantidadProducto : 0;

        if (qty <= 0 || currentQty + qty > stock) {
            setError(`Stock insuficiente para ${prod.nombreProducto}. Disponible: ${stock}.`);
            return;
        }

        if (existing) {
            setCart(prev =>
                prev.map(item =>
                    item.idProducto === prod.idProducto
                        ? { ...item, cantidadProducto: item.cantidadProducto + qty }
                        : item
                )
            );
        } else {
            setCart(prev => [
                ...prev,
                {
                    idProducto: prod.idProducto,
                    nombreProducto: prod.nombreProducto,
                    precioProducto: Number(prod.precioProducto ?? 0),
                    cantidadProducto: qty,
                },
            ]);
        }

        setSelectedProductId('');
        setQuantity(1);
    }

    function handleRemoveItem(idProducto) {
        setCart(prev => prev.filter(item => item.idProducto !== idProducto));
    }

    const totalCart = useMemo(
        () => cart.reduce((acc, item) => acc + item.precioProducto * item.cantidadProducto, 0),
        [cart]
    );

    async function handleConfirmInStoreSale() {
        if (cart.length === 0) return;
        setSubmittingSale(true);
        setError('');
        setFeedback('');
        setLastSale(null);

        try {
            const created = await createInStoreOrder({
                metodoPagoPedido: paymentMethod,
                direccionCliente: 'Venta presencial en tienda',
                detalles: cart.map(item => ({
                    idProducto: item.idProducto,
                    cantidadProducto: item.cantidadProducto,
                })),
            });

            const comprobante = created?.numeroComprobantePedido
                ? `${created.serieComprobantePedido}-${created.numeroComprobantePedido}`
                : '';

            setLastSale({
                id: created?.idPedido,
                comprobante,
                total: Number(created?.montoTotalPedido ?? totalCart),
            });
            setCart([]);
            setFeedback(
                `Venta presencial registrada en estado COMPLETADO.${comprobante ? ` Comprobante: ${comprobante}.` : ''}`
            );
            await loadCatalog();
        } catch (err) {
            setError(err.message || 'No se pudo registrar la venta presencial.');
        } finally {
            setSubmittingSale(false);
        }
    }

    const filteredServices = useMemo(() => {
        const q = serviceSearch.toLowerCase().trim();
        if (!q) return services;
        return services.filter(s =>
            `${s.nombreServicio ?? ''} ${s.categoriaServicio ?? ''} ${s.tipoServicio ?? ''}`
                .toLowerCase()
                .includes(q)
        );
    }, [services, serviceSearch]);

    const availableProductsCount = useMemo(
        () => products.filter(p => Number(p.stockProducto ?? 0) > 0 && p.estadoProducto !== 'INACTIVO').length,
        [products]
    );

    return (
        <>
            <SectionHeader
                icon={Store}
                title="Mostrador y Catálogo"
                description="Registra ventas presenciales directas (sin repartidor, estado COMPLETADO) y consulta el catálogo disponible."
            />

            <Summary
                items={[
                    ['Productos disponibles', availableProductsCount],
                    ['Total productos', products.length],
                    ['Servicios activos', services.length],
                ]}
            />

            <div className="mb-5 flex flex-wrap gap-2">
                <Button
                    type="button"
                    variant={tab === 'pos' ? 'primary' : 'outline'}
                    size="sm"
                    onClick={() => setTab('pos')}
                >
                    <Store size={15} className="mr-1.5" />
                    Venta presencial
                </Button>
                <Button
                    type="button"
                    variant={tab === 'products' ? 'primary' : 'outline'}
                    size="sm"
                    onClick={() => setTab('products')}
                >
                    <Package size={15} className="mr-1.5" />
                    Catálogo de productos
                </Button>
                <Button
                    type="button"
                    variant={tab === 'services' ? 'primary' : 'outline'}
                    size="sm"
                    onClick={() => setTab('services')}
                >
                    <Wrench size={15} className="mr-1.5" />
                    Catálogo de servicios
                </Button>
            </div>

            {error && (
                <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                    {error}
                </div>
            )}
            {feedback && (
                <div className="mb-4 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                    <span>{feedback}</span>
                    {lastSale?.id && (
                        <ReceiptButton
                            variant="primary"
                            disponible={Boolean(lastSale.comprobante)}
                            onClick={() => setReceipt(lastSale)}
                        />
                    )}
                </div>
            )}

            {tab === 'pos' && (
                <Panel>
                    <div className="space-y-6">
                        <div className="grid gap-4 rounded-xl bg-gray-50 p-4 text-sm sm:grid-cols-3">
                            <div>
                                <span className="text-xs text-gray-500">Vendedor responsable</span>
                                <p className="mt-0.5 font-semibold text-gray-800">{sellerName}</p>
                            </div>
                            <div>
                                <span className="text-xs text-gray-500">Modalidad</span>
                                <p className="mt-0.5 font-semibold text-gray-800">Presencial en local (Sin repartidor)</p>
                            </div>
                            <div>
                                <span className="text-xs text-gray-500">Estado al registrar</span>
                                <div className="mt-0.5">
                                    <Badge variant="success">COMPLETADO</Badge>
                                </div>
                            </div>
                        </div>

                        <form onSubmit={handleAddItem} className="grid grid-cols-1 gap-3 sm:grid-cols-12 sm:items-end">
                            <div className="sm:col-span-7">
                                <Field label="Producto">
                                    <Select
                                        required
                                        placeholder="Seleccionar producto disponible..."
                                        value={selectedProductId}
                                        onChange={e => setSelectedProductId(e.target.value)}
                                        options={products
                                            .filter(p => p.estadoProducto !== 'INACTIVO' && Number(p.stockProducto ?? 0) > 0)
                                            .map(p => ({
                                                value: p.idProducto,
                                                label: `${p.nombreProducto} — ${money(p.precioProducto)} (Stock: ${p.stockProducto})`,
                                            }))}
                                    />
                                </Field>
                            </div>

                            <div className="sm:col-span-2">
                                <Field label="Cantidad">
                                    <input
                                        type="number"
                                        min={1}
                                        required
                                        className="bo-input w-full"
                                        value={quantity}
                                        onChange={e => setQuantity(e.target.value)}
                                    />
                                </Field>
                            </div>

                            <div className="sm:col-span-3">
                                <Button type="submit" variant="outline" className="w-full justify-center">
                                    <Plus size={16} className="mr-1.5" />
                                    Agregar
                                </Button>
                            </div>
                        </form>

                        {cart.length > 0 ? (
                            <div className="space-y-4">
                                <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200/80 text-sm">
                                    {cart.map(item => (
                                        <li key={item.idProducto} className="flex items-center justify-between gap-3 px-4 py-3">
                                            <div>
                                                <p className="font-medium text-gray-800">{item.nombreProducto}</p>
                                                <p className="text-xs text-gray-500">
                                                    {item.cantidadProducto} × {money(item.precioProducto)}
                                                </p>
                                            </div>
                                            <div className="flex items-center gap-3">
                                                <span className="font-semibold text-gray-900">
                                                    {money(item.precioProducto * item.cantidadProducto)}
                                                </span>
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveItem(item.idProducto)}
                                                    className="rounded-lg p-1.5 text-red-600 hover:bg-red-50"
                                                    title="Quitar producto"
                                                >
                                                    <Trash2 size={15} />
                                                </button>
                                            </div>
                                        </li>
                                    ))}
                                </ul>

                                <div className="grid gap-4 sm:grid-cols-2 sm:items-end">
                                    <Field label="Método de pago">
                                        <select
                                            className="bo-input"
                                            value={paymentMethod}
                                            onChange={e => setPaymentMethod(e.target.value)}
                                        >
                                            {['EFECTIVO', 'YAPE', 'PLIN', 'TARJETA', 'TRANSFERENCIA'].map(m => (
                                                <option key={m} value={m}>
                                                    {m}
                                                </option>
                                            ))}
                                        </select>
                                    </Field>

                                    <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                                        <span className="text-sm text-gray-600">Total a cobrar</span>
                                        <span className="text-xl font-bold text-gray-900">{money(totalCart)}</span>
                                    </div>
                                </div>

                                <div className="flex justify-end">
                                    <Button
                                        type="button"
                                        disabled={submittingSale}
                                        onClick={handleConfirmInStoreSale}
                                    >
                                        <CheckCircle2 size={16} className="mr-2" />
                                        {submittingSale ? 'Registrando venta...' : 'Confirmar venta presencial'}
                                    </Button>
                                </div>
                            </div>
                        ) : (
                            <p className="rounded-xl border border-dashed border-gray-200 py-8 text-center text-sm text-gray-500">
                                Agrega productos arriba para registrar una venta presencial en mostrador.
                            </p>
                        )}
                    </div>
                </Panel>
            )}

            {tab === 'products' && (
                <Panel>
                    <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
                        <SearchBox
                            value={productSearch}
                            onChange={setProductSearch}
                            placeholder="Buscar producto por nombre o código..."
                        />
                        <Button type="button" variant="outline" size="sm" onClick={loadCatalog}>
                            <RefreshCw size={14} className="mr-1.5" />
                            Actualizar
                        </Button>
                    </div>

                    <DataTable
                        emptyText={loading ? 'Cargando productos...' : 'No hay productos disponibles.'}
                        rows={products}
                        columns={[
                            { key: 'codigoProducto', label: 'Código', render: p => p.codigoProducto || '-' },
                            { key: 'nombreProducto', label: 'Producto' },
                            { key: 'categoriaProducto', label: 'Categoría', render: p => p.categoriaProducto || '-' },
                            { key: 'precioProducto', label: 'Precio', render: p => money(p.precioProducto) },
                            {
                                key: 'stockProducto',
                                label: 'Stock disponible',
                                render: p => {
                                    const stock = Number(p.stockProducto ?? 0);
                                    const min = Number(p.stockMinimo ?? 0);
                                    return (
                                        <Badge variant={stock === 0 ? 'danger' : stock <= min ? 'warning' : 'success'}>
                                            {stock} unid.
                                        </Badge>
                                    );
                                },
                            },
                            {
                                key: 'estadoProducto',
                                label: 'Estado',
                                render: p => (
                                    <Badge variant={p.estadoProducto === 'INACTIVO' ? 'danger' : 'info'}>
                                        {p.estadoProducto || 'ACTIVO'}
                                    </Badge>
                                ),
                            },
                        ]}
                    />
                </Panel>
            )}

            {tab === 'services' && (
                <Panel>
                    <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
                        <SearchBox
                            value={serviceSearch}
                            onChange={setServiceSearch}
                            placeholder="Buscar servicio o categoría..."
                        />
                        <Button type="button" variant="outline" size="sm" onClick={loadCatalog}>
                            <RefreshCw size={14} className="mr-1.5" />
                            Actualizar
                        </Button>
                    </div>

                    <DataTable
                        emptyText={loading ? 'Cargando servicios...' : 'No hay servicios disponibles.'}
                        rows={filteredServices}
                        columns={[
                            { key: 'nombreServicio', label: 'Servicio' },
                            { key: 'categoriaServicio', label: 'Categoría', render: s => s.categoriaServicio || '-' },
                            { key: 'tipoServicio', label: 'Tipo', render: s => s.tipoServicio || '-' },
                            { key: 'duracionEstimadaServicio', label: 'Duración', render: s => s.duracionEstimadaServicio || '-' },
                            { key: 'cantidadPersonalServicio', label: 'Personal req.', render: s => s.cantidadPersonalServicio || '1' },
                            { key: 'precioServicio', label: 'Tarifa', render: s => money(s.precioServicio) },
                        ]}
                    />
                </Panel>
            )}

            {receipt && (
                <ReceiptModal
                    kind="pedido"
                    id={receipt.id}
                    numero={receipt.comprobante}
                    onClose={() => setReceipt(null)}
                />
            )}
        </>
    );
}