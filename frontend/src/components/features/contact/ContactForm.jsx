import { useState } from "react";
import { User, Mail, MessageCircle } from "lucide-react";
import Card from "@/components/ui/Card";
import Input from "@/components/ui/Input";
import Textarea from "@/components/ui/Textarea";
import Button from "@/components/ui/Button";
import PageTitle from "@/components/shared/PageTitle";

const INITIAL_FORM = { name: "", email: "", message: "" };

export default function ContactForm() {
    const [form, setForm] = useState(INITIAL_FORM);
    const [sent, setSent] = useState(false);

    // Un solo manejador para todos los campos: usa el atributo name de cada uno
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
        setSent(false);
    };

    const handleSubmit = (e) => {
        e.preventDefault(); // evita que el navegador recargue la página
        // TODO: enviar `form` al backend (por ejemplo en services/contactService.js)
        setSent(true);
        setForm(INITIAL_FORM);
    };

    return (
        <Card className="p-6 md:p-8">
            <PageTitle as="h2" size="md">Envíanos un mensaje</PageTitle>

            <form onSubmit={handleSubmit} className="mt-6 space-y-4">
                <Input
                    icon={User}
                    name="name"
                    value={form.name}
                    onChange={handleChange}
                    placeholder="Nombres y Apellidos"
                    aria-label="Nombres y apellidos"
                    autoComplete="name"
                    required
                />
                <Input
                    icon={Mail}
                    type="email"
                    name="email"
                    value={form.email}
                    onChange={handleChange}
                    placeholder="Correo electrónico"
                    aria-label="Correo electrónico"
                    autoComplete="email"
                    required
                />
                <Textarea
                    icon={MessageCircle}
                    name="message"
                    value={form.message}
                    onChange={handleChange}
                    placeholder="Mensaje / Consulta"
                    aria-label="Mensaje o consulta"
                    required
                />

                <div className="text-center">
                    <Button type="submit" size="lg">Enviar</Button>
                </div>

                {sent && (
                    <p role="status" className="text-center text-sm font-medium text-brand">
                        ¡Gracias! Recibimos tu mensaje.
                    </p>
                )}
            </form>
        </Card>
    );
}