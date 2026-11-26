import { useEffect, useRef, useState } from 'react';
import { Building2, CheckCircle2, Mail, MessageCircle, Phone, Send, User } from 'lucide-react';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Textarea from '@/components/ui/Textarea';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import PageTitle from '@/components/shared/PageTitle';

const REASONS = [
    { value: 'general', label: 'Consulta general' },
    { value: 'servicio', label: 'Consulta sobre un servicio' },
    { value: 'mayor-alcance', label: 'Servicio de mayor alcance' },
    { value: 'contrato', label: 'Contrato por un periodo definido' },
];
const EMPTY = { name: '', email: '', phone: '', company: '', location: '', area: '', duration: '', frequency: 'semanal', message: '' };

function Field({ label, id, children }) {
    return <div><label htmlFor={id} className="mb-2 block text-sm font-medium text-gray-700">{label}</label>{children}</div>;
}

export default function ContactForm({ initialReason = 'general' }) {
    const reason = REASONS.some(option => option.value === initialReason) ? initialReason : 'general';
    const [form, setForm] = useState({ ...EMPTY, reason });
    const [sending, setSending] = useState(false);
    const [sent, setSent] = useState(false);
    const [validation, setValidation] = useState('');
    const dialog = useRef(null);
    const timer = useRef(null);
    const project = form.reason !== 'general';
    const contract = form.reason === 'contrato';
    const change = event => { setForm(previous => ({ ...previous, [event.target.name]: event.target.value })); setValidation(''); };
    useEffect(() => () => clearTimeout(timer.current), []);
    useEffect(() => { if (sent && !dialog.current.open) dialog.current.showModal(); }, [sent]);

    function submit(event) {
        event.preventDefault();
        if (sending) return;
        if (![form.name, form.email, form.message, ...(project ? [form.location] : []), ...(contract ? [form.company] : [])].every(value => value.trim())) {
            setValidation('Completa los campos obligatorios con información válida.');
            return;
        }
        setSending(true);
        // Simulación del prototipo: no envía datos a un servidor ni a un servicio de correo.
        timer.current = setTimeout(() => {
            setSending(false);
            setSent(true);
            setForm({ ...EMPTY, reason: form.reason });
        }, 600);
    }

    return <Card className="p-6 md:p-8">
        <PageTitle as="h2" size="md">Envíanos un mensaje</PageTitle>
        <p className="mt-2 text-sm leading-6 text-gray-500">Cuéntanos qué necesitas para orientarte mejor.</p>
        <form onSubmit={submit} className="mt-6 space-y-5">
            <fieldset disabled={sending} className="space-y-5 disabled:opacity-70">
                <Field id="contact-reason" label="Motivo de contacto">
                    <Select disabled={sending} id="contact-reason" aria-label="Motivo de contacto" options={REASONS} value={form.reason} onChange={event => { setForm(previous => ({ ...previous, reason: event.target.value })); setValidation(''); }} />
                </Field>
                <Field id="contact-name" label="Nombres y apellidos *"><Input id="contact-name" icon={User} name="name" value={form.name} onChange={change} placeholder="Tu nombre completo" autoComplete="name" maxLength={150} required /></Field>
                <Field id="contact-email" label="Correo electrónico *"><Input id="contact-email" icon={Mail} type="email" name="email" value={form.email} onChange={change} placeholder="nombre@correo.com" autoComplete="email" maxLength={150} required /></Field>
                <Field id="contact-phone" label="Teléfono de contacto (opcional)"><Input id="contact-phone" icon={Phone} type="tel" name="phone" value={form.phone} onChange={change} placeholder="Número de contacto" autoComplete="tel" maxLength={25} /></Field>
                {project && <div className="space-y-4 rounded-xl border border-blue-100 bg-blue-50/50 p-4">
                    <h3 className="text-sm font-semibold text-brand">{contract ? 'Datos del contrato' : 'Datos del servicio'}</h3>
                    <Field id="contact-company" label={`Empresa u organización${contract ? ' *' : ' (opcional)'}`}><Input id="contact-company" icon={Building2} name="company" value={form.company} onChange={change} autoComplete="organization" maxLength={150} required={contract} /></Field>
                    <Field id="contact-location" label="Ubicación del servicio *"><Input id="contact-location" name="location" value={form.location} onChange={change} placeholder="Dirección o distrito" maxLength={255} required /></Field>
                    <Field id="contact-area" label="Área aproximada en m² (opcional)"><Input id="contact-area" type="number" min="1" step="any" name="area" value={form.area} onChange={change} placeholder="Ej. 250" /></Field>
                    {contract && <>
                        <Field id="contact-duration" label="Duración del contrato en meses *"><Input id="contact-duration" type="number" min="1" step="1" name="duration" value={form.duration} onChange={change} placeholder="Ej. 6" required /></Field>
                        <Field id="contact-frequency" label="Frecuencia de atención"><Select disabled={sending} id="contact-frequency" aria-label="Frecuencia de atención" value={form.frequency} onChange={event => setForm(previous => ({ ...previous, frequency: event.target.value }))} options={[{ value: 'diaria', label: 'Diaria' }, { value: 'semanal', label: 'Semanal' }, { value: 'quincenal', label: 'Quincenal' }, { value: 'mensual', label: 'Mensual' }, { value: 'coordinar', label: 'Por coordinar' }]} /></Field>
                    </>}
                </div>}
                <Field id="contact-message" label="Mensaje o consulta *"><Textarea id="contact-message" icon={MessageCircle} name="message" value={form.message} onChange={change} placeholder="Describe lo que necesitas..." maxLength={3000} required /></Field>
            </fieldset>
            {validation && <p role="alert" className="text-sm text-red-600">{validation}</p>}
            <Button type="submit" disabled={sending} className="w-full gap-2"><Send size={17} aria-hidden="true" />{sending ? 'Enviando…' : 'Enviar mensaje'}</Button>
        </form>
        <dialog ref={dialog} onClose={() => setSent(false)} aria-labelledby="contact-sent-title" aria-describedby="contact-sent-description" className="m-auto w-[calc(100%_-_2rem)] max-w-md rounded-2xl border border-gray-200 p-7 text-center shadow-xl backdrop:bg-slate-900/45">
            <CheckCircle2 size={52} className="mx-auto mb-4 text-brand" aria-hidden="true" />
            <h2 id="contact-sent-title" className="text-2xl font-semibold text-gray-900">¡Mensaje enviado!</h2>
            <p id="contact-sent-description" className="mt-3 text-sm leading-7 text-gray-500">Esta es una simulación del prototipo. No se ha enviado ningún correo real.</p>
            <Button autoFocus onClick={() => dialog.current.close()} className="mt-6 w-full">Entendido</Button>
        </dialog>
    </Card>;
}
