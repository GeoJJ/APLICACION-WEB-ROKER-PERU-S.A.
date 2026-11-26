import Breadcrumb from '@/components/shared/Breadcrumb';
import ContactInfo from '@/components/features/contact/ContactInfo';
import ContactForm from '@/components/features/contact/ContactForm';
import { useSearchParams } from 'react-router-dom';

export default function ContactPage() {
    const [params] = useSearchParams();
    const reason = params.get('motivo') || 'general';
    return <div className="mx-auto max-w-[1280px] px-4 py-8 md:px-6">
        <Breadcrumb items={[{ label: 'Contacto' }]} />
        <h1 className="sr-only">Contacto</h1>
        <div className="mt-6 grid items-start gap-6 lg:grid-cols-2">
            <ContactInfo />
            <ContactForm key={reason} initialReason={reason} />
        </div>
    </div>;
}
