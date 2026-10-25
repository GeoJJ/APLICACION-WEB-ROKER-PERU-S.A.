import Breadcrumb from "@/components/shared/Breadcrumb";
import ContactInfo from "@/components/features/contact/ContactInfo";
import ContactForm from "@/components/features/contact/ContactForm";

export default function ContactPage() {
    return (
        <div className="mx-auto max-w-[1280px] px-6 py-8">
            <Breadcrumb items={[{ label: "Contacto" }]} />
            {/* Título oculto visualmente, pero leído por lectores de pantalla */}
            <h1 className="sr-only">Contacto</h1>

            <div className="mt-4 grid gap-6 lg:grid-cols-[2fr_1fr]">
                <ContactInfo />
                <ContactForm />
            </div>
        </div>
    );
}