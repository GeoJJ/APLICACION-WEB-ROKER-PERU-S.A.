import { MapPin, Phone, Mail } from "lucide-react";
import Card from "@/components/ui/Card";
import PageTitle from "@/components/shared/PageTitle";
import InfoItem from "@/components/shared/InfoItem";
import { CONTACT } from "@/data/contact";

export default function ContactInfo() {
    const phoneHref = `tel:${CONTACT.phone.replace(/\s/g, "")}`;
    const mapSrc = `https://www.google.com/maps?q=${encodeURIComponent(CONTACT.address)}&output=embed`;

    return (
        <Card className="p-6 md:p-8">
            <PageTitle as="h2">Encuéntranos</PageTitle>

            <div className="mt-6 grid gap-8 md:grid-cols-2">
                <div className="space-y-6">
                    <InfoItem icon={MapPin} title="Dirección">
                        {CONTACT.address}
                    </InfoItem>
                    <InfoItem icon={Phone} title="Teléfono">
                        <a href={phoneHref} className="hover:underline">{CONTACT.phone}</a>
                    </InfoItem>
                    <InfoItem icon={Mail} title="Email">
                        <a href={`mailto:${CONTACT.email}`} className="break-all hover:underline">
                            {CONTACT.email}
                        </a>
                    </InfoItem>
                </div>

                <iframe
                    title="Ubicación de la empresa en el mapa"
                    src={mapSrc}
                    loading="lazy"
                    className="h-72 w-full rounded-xl border-0 md:h-full md:min-h-72"
                />
            </div>
        </Card>
    );
}