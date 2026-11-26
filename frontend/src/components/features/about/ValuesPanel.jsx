import InfoItem from "@/components/shared/InfoItem";
import { VALUES } from "@/data/about";

export default function ValuesPanel() {
    return (
        <aside className="rounded-2xl bg-brand/5 p-6">
            <h2 className="text-2xl font-semibold">Nuestros valores</h2>

            <div className="mt-6 space-y-5">
                {VALUES.map((value) => (
                    <InfoItem key={value.id} icon={value.icon} title={value.title} circle={false}>
                        {value.text}
                    </InfoItem>
                ))}
            </div>

            <a href="#certificaciones" className="mt-8 inline-block text-sm font-semibold text-brand hover:underline">Conoce nuestras certificaciones</a>
        </aside>
    );
}