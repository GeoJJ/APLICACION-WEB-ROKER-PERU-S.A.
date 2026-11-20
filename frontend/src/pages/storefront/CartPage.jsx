import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, Truck, CheckCircle2 } from 'lucide-react';
import { useCart } from '@/components/cart/CartProvider';
import {
    createClientOrder,
    fetchClientProfile,
    updateClientProfile,
} from '@/lib/clienteApi';
import CartItems from '@/components/cart/CartItems';
import { money } from '@/data/backofficeStore';
import { Field, Panel } from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import Breadcrumb from '@/components/shared/Breadcrumb';

function mapProfileFields(data) {
    if (!data) {
        return {
            nombreUsuario: '',
            correoUsuario: '',
            telefonoUsuario: '',
            numeroDocumentoUsuario: '',
            direccionUsuario: '',
        };
    }

    return {
        nombreUsuario: data.nombreUsuario ?? data.nombre ?? '',
        correoUsuario: data.correoUsuario ?? data.email ?? '',
        telefonoUsuario: data.telefonoUsuario ?? data.telefono ?? '',
        numeroDocumentoUsuario: data.numeroDocumentoUsuario ?? data.documento ?? '',
        direccionUsuario: data.direccionUsuario ?? data.direccion ?? '',
    };
}

export default function CartPage() {
    const { items, count, subtotal, error: cartError, clear } = useCart();

    const [profile, setProfile] = useState({
        nombreUsuario: '',
        correoUsuario: '',
        telefonoUsuario: '',
        numeroDocumentoUsuario: '',
        direccionUsuario: '',
    });
    const [initialAddress, setInitialAddress] = useState('');
    const [loadingProfile, setLoadingProfile] = useState(true);
    const [metodoPagoPedido, setMetodoPagoPedido] = useState('EFECTIVO');
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    const [createdOrder, setCreatedOrder] = useState(null);

    useEffect(() => {
        let active = true;

        async function loadProfile() {
            setLoadingProfile(true);
            try {
                const data = await fetchClientProfile();
                if (!active) return;
                const mapped = mapProfileFields(data);
                setProfile(mapped);
                setInitialAddress(mapped.direccionUsuario);
            } catch {
                // Si no se puede cargar el perfil, dejamos los campos vacíos sin bloquear el carrito
            } finally {
                if (active) setLoadingProfile(false);
            }
        }

        loadProfile();
        return () => {
            active = false;
        };
    }, []);

    const hasInvalidItem = items.some(
        p => !p.available || (p.stock != null && p.quantity > p.stock)
    );

    async function handleConfirmOrder(e) {
        e.preventDefault();
        if (!items.length || hasInvalidItem || submitting) return;

        setSubmitting(true);
        setSubmitError('');

        try {
            const trimmedAddress = profile.direccionUsuario.trim();
            if (trimmedAddress && trimmedAddress !== initialAddress.trim()) {
                await updateClientProfile({
                    ...profile,
                    direccionUsuario: trimmedAddress,
                });
                setInitialAddress(trimmedAddress);
            }

            const payload = {
                metodoPagoPedido,
                detalles: items.map(p => ({
                    idProducto: p.id,
                    cantidadProducto: p.quantity,
                })),
            };

            const res = await createClientOrder(payload);
            setCreatedOrder(res || { ok: true });
            clear();
        } catch (err) {
            setSubmitError(err?.message || 'No se pudo registrar el pedido.');
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="bg-gray-50">
            <div className="mx-auto max-w-[1280px] px-4 py-10 md:px-6">
                <Breadcrumb items={[{ label: 'Productos', href: '/productos' }, { label: 'Carrito' }]} />
                <h1 className="mb-3 mt-5 flex items-center gap-3 text-3xl font-semibold">
                    <ShoppingBag className="text-brand" />
                    Tu carrito
                </h1>
                <p className="mb-8 text-sm text-gray-500">
                    Revisa tu selección y las cantidades antes de continuar.
                </p>

                {cartError && (
                    <p role="alert" className="mb-5 text-sm text-red-700">
                        {cartError}
                    </p>
                )}

                {createdOrder ? (
                    <Panel className="py-14 text-center">
                        <CheckCircle2 size={46} className="mx-auto text-emerald-600" />
                        <h2 className="mt-4 text-2xl font-semibold text-gray-900">
                            ¡Pedido registrado!
                        </h2>
                        <p className="mx-auto mt-2 max-w-md text-sm text-gray-600">
                            {createdOrder.idPedido
                                ? `Tu pedido fue enviado correctamente.`
                                : 'Tu pedido fue enviado correctamente para su confirmación.'}
                        </p>
                        <div className="mt-6 flex flex-wrap justify-center gap-3">
                            <Button as={Link} to="/productos">
                                Seguir comprando
                            </Button>
                        </div>
                    </Panel>
                ) : !items.length ? (
                    <Panel className="py-16 text-center">
                        <ShoppingBag size={44} className="mx-auto text-brand" />
                        <h2 className="mt-5 text-xl font-semibold">Tu carrito está vacío</h2>
                        <p className="mb-6 mt-3 text-sm text-gray-500">
                            Agrega productos desde el catálogo.
                        </p>
                        <Button as={Link} to="/productos">
                            Explorar productos
                        </Button>
                    </Panel>
                ) : (
                    <form onSubmit={handleConfirmOrder} className="grid items-start gap-6 lg:grid-cols-[1.25fr_1fr]">
                        <Panel>
                            <h2 className="border-b border-gray-200 pb-4 text-lg font-semibold">
                                Productos seleccionados · {count} unidades
                            </h2>
                            <CartItems />
                            <Link to="/productos" className="mt-4 inline-block text-sm font-semibold text-brand">
                                Seguir comprando
                            </Link>
                        </Panel>

                        <div className="grid gap-6">
                            <Panel>
                                <h2 className="mb-5 text-lg font-semibold">Resumen de compra</h2>
                                <div className="flex justify-between">
                                    <span>Subtotal</span>
                                    <strong>{money(subtotal)}</strong>
                                </div>
                                <p className="mt-4 flex gap-2 text-xs leading-5 text-gray-500">
                                    <Truck size={17} className="shrink-0" />
                                    El pedido se registrará con los datos de tu cuenta y será coordinado por el equipo.
                                </p>
                            </Panel>

                            <Panel>
                                <div className="mb-5 flex items-center justify-between gap-2">
                                    <h2 className="text-lg font-semibold">Datos de entrega y pago</h2>
                                    <Link to="/perfil" className="text-xs font-semibold text-brand hover:underline">
                                        Editar en perfil
                                    </Link>
                                </div>

                                <div className="grid gap-4 sm:grid-cols-2">
                                    <Field label="Nombre">
                                        <input
                                            readOnly
                                            disabled={loadingProfile}
                                            className="bo-input bg-gray-50 text-gray-600"
                                            value={loadingProfile ? 'Cargando...' : profile.nombreUsuario}
                                        />
                                    </Field>

                                    <Field label="Teléfono">
                                        <input
                                            readOnly
                                            disabled={loadingProfile}
                                            className="bo-input bg-gray-50 text-gray-600"
                                            value={loadingProfile ? 'Cargando...' : profile.telefonoUsuario}
                                        />
                                    </Field>

                                    <Field label="DNI / Documento">
                                        <input
                                            readOnly
                                            disabled={loadingProfile}
                                            className="bo-input bg-gray-50 text-gray-600"
                                            value={loadingProfile ? 'Cargando...' : profile.numeroDocumentoUsuario}
                                        />
                                    </Field>

                                    <Field label="Método de pago">
                                        <Select
                                            required
                                            value={metodoPagoPedido}
                                            onChange={e => setMetodoPagoPedido(e.target.value)}
                                            options={[
                                                { value: 'EFECTIVO', label: 'Efectivo' },
                                                { value: 'YAPE', label: 'Yape' },
                                                { value: 'PLIN', label: 'Plin' },
                                                { value: 'TRANSFERENCIA', label: 'Transferencia' },
                                            ]}
                                        />
                                    </Field>

                                    <div className="sm:col-span-2">
                                        <Field label="Dirección de entrega">
                                            <input
                                                type="text"
                                                required
                                                disabled={loadingProfile || submitting}
                                                className="bo-input"
                                                placeholder="Ingresa tu dirección de entrega"
                                                value={loadingProfile ? '' : profile.direccionUsuario}
                                                onChange={e =>
                                                    setProfile(prev => ({
                                                        ...prev,
                                                        direccionUsuario: e.target.value,
                                                    }))
                                                }
                                            />
                                        </Field>
                                    </div>
                                </div>

                                {hasInvalidItem && (
                                    <p role="alert" className="mt-4 text-xs font-medium text-red-600">
                                        Revisa los productos con stock insuficiente o no disponibles antes de continuar.
                                    </p>
                                )}

                                {submitError && (
                                    <p role="alert" className="mt-4 rounded-xl bg-red-50 p-3 text-xs text-red-700">
                                        {submitError}
                                    </p>
                                )}

                                <Button
                                    type="submit"
                                    className="mt-5 w-full"
                                    disabled={submitting || hasInvalidItem || loadingProfile}
                                >
                                    {submitting ? 'Confirmando pedido...' : 'Confirmar pedido'}
                                </Button>
                            </Panel>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
}