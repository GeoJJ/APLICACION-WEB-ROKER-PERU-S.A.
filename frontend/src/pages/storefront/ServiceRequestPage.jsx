import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { CalendarDays, CheckCircle2, ClipboardList, MapPin, Wallet } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { useCatalog } from '@/hooks/useCatalog';
import { createClientServiceOrder, fetchClientServiceOrders } from '@/lib/clienteApi';
import { money } from '@/lib/format';
import { localDate } from '@/data/backofficeStore';
import { Field, Panel } from '@/components/shared/ManagementUI';
import Breadcrumb from '@/components/shared/Breadcrumb';
import Button from '@/components/ui/Button';

const EMPTY_FORM = {
    ambiente: 'Oficina',
    area: '',
    ambientes: '1',
    frecuencia: 'Única',
    necesidades: '',
    direccion: '',
    distrito: '',
    referencia: '',
    fecha: '',
    hora: '',
    metodoPago: 'EFECTIVO',
};

const PAYMENT_METHODS = [
    { value: 'EFECTIVO', label: 'Efectivo' },
    { value: 'YAPE', label: 'Yape' },
    { value: 'PLIN', label: 'Plin' },
    { value: 'TRANSFERENCIA', label: 'Transferencia' },
];

// Si el admin guardó solo un número (ej. "2"), se muestra con su unidad.
const withUnit = (value, unit) => (/^\d+([.,]\d+)?$/.test(String(value ?? '').trim()) ? `${String(value).trim()} ${unit}` : value);

// Guarda el borrador en la sesión para no perder lo escrito si se recarga la página.
function draftFor(id) {
    try {
        return { ...EMPTY_FORM, ...(JSON.parse(sessionStorage.getItem('roker-service-' + id)) || {}) };
    } catch {
        return { ...EMPTY_FORM };
    }
}

export default function ServiceRequestPage() {
    const { id } = useParams();
    const { user } = useAuth();
    const { items, loading, error: catalogError } = useCatalog('services');
    const service = items.find(s => String(s.id) === id);

    const [form, setForm] = useState(() => draftFor(id));
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState('');
    const [sent, setSent] = useState(null);

    useEffect(() => {
        if (sent) return;
        try {
            sessionStorage.setItem('roker-service-' + id, JSON.stringify(form));
        } catch {
            /* el formulario funciona igual sin almacenamiento */
        }
    }, [form, id, sent]);

    async function submit(e) {
        e.preventDefault();
        if (busy) return;
        setError('');

        const programada = `${form.fecha}T${form.hora}:00`;
        if (new Date(programada).getTime() <= Date.now()) {
            setError('Selecciona una fecha y hora futuras.');
            return;
        }

        // El backend guarda la dirección y un detalle de texto libre; aquí se arma con lo que llenó el cliente.
        const detalle = [
            `Tipo de ambiente: ${form.ambiente}`,
            `Área aproximada: ${form.area} m²`,
            `Cantidad de ambientes: ${form.ambientes}`,
            `Frecuencia: ${form.frecuencia}`,
            form.referencia.trim() && `Referencia: ${form.referencia.trim()}`,
            `Necesidades: ${form.necesidades.trim()}`,
        ].filter(Boolean).join('\n');

        setBusy(true);
        try {
            const result = await createClientServiceOrder({
                idServicio: Number(id),
                fechaProgramadaServicio: programada,
                metodoPagoServicio: form.metodoPago,
                direccionServicio: `${form.direccion.trim()}, ${form.distrito.trim()}`,
                detalleSolicitudServicio: detalle,
            });
            // Número propio del cliente: cuántas solicitudes de servicio lleva (la nueva incluida), sin usar el id de la BD.
            let numero = null;
            try {
                const mine = await fetchClientServiceOrders();
                if (Array.isArray(mine)) numero = mine.length;
            } catch {
                /* si falla la consulta, se muestra el mensaje sin número */
            }
            setSent({ ...result, numero });
            sessionStorage.removeItem('roker-service-' + id);
        } catch (err) {
            setError(err.message);
        } finally {
            setBusy(false);
        }
    }

    function field(key, label, type = 'text', extra = {}) {
        return (
            <Field key={key} label={label}>
                <input
                    className="bo-input"
                    required={key !== 'referencia'}
                    type={type}
                    value={form[key]}
                    onChange={e => setForm({ ...form, [key]: e.target.value })}
                    {...extra}
                />
            </Field>
        );
    }

    // ---- Pantallas intermedias -------------------------------------------------
    let body;
    if (loading) {
        body = <p className="py-12 text-gray-500">Cargando servicio...</p>;
    } else if (!service) {
        body = (
            <Panel className="mt-8">
                <p role="alert">{catalogError || 'Este servicio no está disponible.'}</p>
                <Link to="/servicios" className="mt-4 inline-block text-brand underline">Volver a servicios</Link>
            </Panel>
        );
    } else if (sent) {
        body = (
            <Panel className="mx-auto mt-8 max-w-2xl py-12 text-center">
                <CheckCircle2 size={48} className="mx-auto text-green-600" />
                <h1 className="mt-5 text-2xl font-semibold">Solicitud enviada</h1>
                <p className="mt-3 text-brand">{sent.numero ? `Solicitud #${sent.numero} · ` : ''}{service.name}</p>
                <p className="mx-auto mb-6 mt-4 max-w-lg text-sm leading-6 text-gray-500">
                    Tu solicitud quedó pendiente de confirmación. Un vendedor asignará al personal y la confirmará;
                    cuando eso ocurra podrás descargar tu comprobante desde Mis solicitudes.
                </p>
                <Button as={Link} to="/mis-solicitudes?tab=servicios">Ver mis solicitudes</Button>
            </Panel>
        );
    } else if (!user) {
        body = (
            <Panel className="mx-auto mt-8 max-w-2xl py-12 text-center">
                <h1 className="text-2xl font-semibold">Inicia sesión para solicitar este servicio</h1>
                <p className="mx-auto mb-6 mt-4 max-w-lg text-sm leading-6 text-gray-500">
                    Necesitamos tu cuenta para registrar la solicitud y mostrarte su estado.
                </p>
                <div className="flex justify-center gap-3">
                    <Button as={Link} to="/login" state={{ from: `/servicios/${id}/solicitar` }}>Iniciar sesión</Button>
                    <Button as={Link} to="/register" variant="secondary">Crear cuenta</Button>
                </div>
            </Panel>
        );
    } else if (user.role !== 'customer') {
        body = (
            <Panel className="mx-auto mt-8 max-w-2xl py-12 text-center">
                <h1 className="text-2xl font-semibold">Solo los clientes pueden solicitar servicios</h1>
                <p className="mx-auto mb-6 mt-4 max-w-lg text-sm leading-6 text-gray-500">
                    Estás conectado con una cuenta del equipo. Para probar la solicitud, entra con una cuenta de cliente.
                </p>
                <Button as={Link} to="/servicios" variant="secondary">Volver a servicios</Button>
            </Panel>
        );
    } else {
        body = (
            <>
                <div className="mb-8 mt-5">
                    <h1 className="text-3xl font-semibold">Solicitar servicio</h1>
                    <p className="mt-3 text-sm text-gray-500">Cuéntanos qué necesitas y cuándo, y lo coordinamos contigo.</p>
                </div>
                <div className="grid items-start gap-6 lg:grid-cols-[1fr_340px]">
                    <form onSubmit={submit} className="grid gap-6">
                        <Panel>
                            <h2 className="mb-5 flex items-center gap-2 text-lg font-semibold"><ClipboardList size={22} className="text-brand" />Tu ambiente</h2>
                            <div className="grid gap-5 sm:grid-cols-2">
                                <Field label="Tipo de ambiente">
                                    <select className="bo-input" value={form.ambiente} onChange={e => setForm({ ...form, ambiente: e.target.value })}>
                                        {['Hogar', 'Oficina', 'Local comercial', 'Almacén', 'Otro'].map(v => <option key={v}>{v}</option>)}
                                    </select>
                                </Field>
                                {field('area', 'Área aproximada (m²)', 'number', { min: 1, max: 1000000, step: '0.1' })}
                                {field('ambientes', 'Cantidad de ambientes', 'number', { min: 1, max: 10000, step: 1 })}
                                <Field label="Frecuencia">
                                    <select className="bo-input" value={form.frecuencia} onChange={e => setForm({ ...form, frecuencia: e.target.value })}>
                                        {['Única', 'Semanal', 'Mensual'].map(v => <option key={v}>{v}</option>)}
                                    </select>
                                </Field>
                            </div>
                            <div className="mt-5">
                                <Field label="Necesidades y condiciones especiales">
                                    <textarea
                                        required
                                        maxLength={2000}
                                        rows={4}
                                        className="bo-input"
                                        placeholder="Describe las áreas a atender, accesos y necesidades del servicio..."
                                        value={form.necesidades}
                                        onChange={e => setForm({ ...form, necesidades: e.target.value })}
                                    />
                                </Field>
                            </div>
                        </Panel>

                        <Panel>
                            <h2 className="mb-5 flex items-center gap-2 text-lg font-semibold"><MapPin size={22} className="text-brand" />Ubicación</h2>
                            <div className="grid gap-5">
                                {field('direccion', 'Dirección', 'text', { maxLength: 150 })}
                                {field('distrito', 'Distrito', 'text', { maxLength: 60 })}
                                {field('referencia', 'Referencia (opcional)', 'text', { maxLength: 200 })}
                            </div>
                        </Panel>

                        <Panel>
                            <h2 className="mb-5 flex items-center gap-2 text-lg font-semibold"><CalendarDays size={22} className="text-brand" />Fecha preferida</h2>
                            <div className="grid gap-5 sm:grid-cols-2">
                                {field('fecha', 'Fecha', 'date', { min: localDate() })}
                                {field('hora', 'Hora preferida', 'time')}
                            </div>
                            <p className="mt-4 text-xs leading-5 text-gray-500">La fecha y hora están sujetas a disponibilidad y se confirman cuando el vendedor aprueba tu solicitud.</p>
                        </Panel>

                        <Panel>
                            <h2 className="mb-5 flex items-center gap-2 text-lg font-semibold"><Wallet size={22} className="text-brand" />Pago</h2>
                            <Field label="Método de pago">
                                <select className="bo-input" value={form.metodoPago} onChange={e => setForm({ ...form, metodoPago: e.target.value })}>
                                    {PAYMENT_METHODS.map(m => <option key={m.value} value={m.value}>{m.label}</option>)}
                                </select>
                            </Field>
                            <p className="mt-4 text-xs leading-5 text-gray-500">
                                Usaremos los datos de contacto de tu cuenta ({user.email}). Puedes actualizarlos en tu perfil.
                            </p>
                        </Panel>

                        {error && <p role="alert" className="rounded-xl bg-red-50 p-4 text-sm text-red-700">{error}</p>}

                        <Panel>
                            <Button type="submit" disabled={busy}>{busy ? 'Enviando...' : 'Enviar solicitud'}</Button>
                        </Panel>
                    </form>

                    <aside className="grid gap-5 lg:sticky lg:top-28">
                        <Panel>
                            <p className="text-xs font-semibold uppercase tracking-widest text-brand">Servicio seleccionado</p>
                            <h2 className="mt-4 text-xl font-semibold">{service.name}</h2>
                            <p className="mt-4 text-sm leading-6 text-gray-500">{service.description}</p>
                            <dl className="mt-5 grid gap-2 text-sm">
                                {service.price != null && <div className="flex justify-between"><dt className="text-gray-500">Tarifa</dt><dd className="font-semibold text-brand">{money(service.price)}</dd></div>}
                                {service.duration && <div className="flex justify-between"><dt className="text-gray-500">Duración estimada</dt><dd className="font-medium">{withUnit(service.duration, 'h')}</dd></div>}
                                {service.personnel && <div className="flex justify-between"><dt className="text-gray-500">Personal</dt><dd className="font-medium">{withUnit(service.personnel, 'personas')}</dd></div>}
                            </dl>
                        </Panel>
                        <Panel>
                            <h3 className="font-semibold">¿Qué sigue?</h3>
                            <ol className="mt-4 list-inside list-decimal space-y-3 text-sm leading-6 text-gray-500">
                                <li>Un vendedor revisa tu solicitud.</li>
                                <li>Asigna al personal y la confirma; ahí se emite tu comprobante.</li>
                                <li>Realizamos el servicio en la fecha programada.</li>
                            </ol>
                        </Panel>
                    </aside>
                </div>
            </>
        );
    }

    return (
        <div className="bg-gray-50">
            <div className="mx-auto max-w-[1280px] px-4 py-10 md:px-6">
                <Breadcrumb items={[{ label: 'Servicios', href: '/servicios' }, { label: 'Solicitar servicio' }]} />
                {body}
            </div>
        </div>
    );
}