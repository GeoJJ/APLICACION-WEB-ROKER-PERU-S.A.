import { MapPin, Phone, Mail, ExternalLink } from 'lucide-react';
import Card from '@/components/ui/Card';
import PageTitle from '@/components/shared/PageTitle';
import InfoItem from '@/components/shared/InfoItem';
import { CONTACT } from '@/data/contact';

export default function ContactInfo() {
    const mapUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(CONTACT.address)}`;
    const mapSrc = `https://www.google.com/maps?q=${encodeURIComponent(CONTACT.address)}&output=embed`;
    return <Card className="p-6 md:p-8">
        <PageTitle as="h2">Encuéntranos</PageTitle>
        <p className="mt-2 text-sm leading-6 text-gray-500">Conversemos sobre tus necesidades de productos, servicios y atención para tu organización.</p>
        <div className="mt-6 space-y-6">
            <InfoItem icon={MapPin} title="Dirección"><a href={mapUrl} target="_blank" rel="noopener noreferrer" className="hover:underline">{CONTACT.address}</a></InfoItem>
            <InfoItem icon={Phone} title="Teléfono"><a href={`tel:${CONTACT.phone.replace(/\s/g, '')}`} className="hover:underline">{CONTACT.phone}</a></InfoItem>
            <InfoItem icon={Mail} title="Email"><a href={`mailto:${CONTACT.email}`} className="break-all hover:underline">{CONTACT.email}</a></InfoItem>
        </div>
        <div className="mt-7 overflow-hidden rounded-xl border border-gray-200">
            <iframe title="Ubicación de la empresa en el mapa" src={mapSrc} loading="lazy" className="h-64 w-full border-0" />
            <a href={mapUrl} target="_blank" rel="noopener noreferrer" className="flex items-center justify-center gap-2 bg-blue-50 px-4 py-3 text-sm font-semibold text-brand hover:bg-blue-100">Cómo llegar<ExternalLink size={16} aria-hidden="true" /></a>
        </div>
    </Card>;
}
