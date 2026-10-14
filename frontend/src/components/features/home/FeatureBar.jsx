import { ShieldCheck, HardHat, Truck, Users } from "lucide-react";

const FEATURES = [
    { icon: ShieldCheck, title: "Productos de calidad", text: "Certificados" },
    { icon: HardHat, title: "Servicios profesionales", text: "Personal capacitado" },
    { icon: Truck, title: "Entrega segura", text: "Delivery rápido y confiable" },
    { icon: Users, title: "Atención rápida", text: "Soporte a clientes" },
];

export default function FeatureBar() {
    return (
        // -mt-10 sube la barra para que se monte sobre el borde inferior del hero
        <section className="relative z-10 mx-auto -mt-10 max-w-[1200px] px-6">
            <ul className="grid gap-6 rounded-2xl bg-white p-6 shadow-xl sm:grid-cols-2 lg:grid-cols-4">
                {FEATURES.map(({ icon: Icon, title, text }) => (
                    <li key={title} className="flex items-center gap-3">
                        <Icon size={40} className="shrink-0 text-brand" />
                        <div>
                            <p className="text-sm font-semibold">{title}</p>
                            <p className="text-xs text-gray-500">{text}</p>
                        </div>
                    </li>
                ))}
            </ul>
        </section>
    );
}