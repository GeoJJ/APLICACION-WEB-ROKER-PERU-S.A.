import { Building2 } from "lucide-react";
import Breadcrumb from "@/components/shared/Breadcrumb";
import PageTitle from "@/components/shared/PageTitle";
import InfoItem from "@/components/shared/InfoItem";
import Card from "@/components/ui/Card";
import ValuesPanel from "@/components/features/about/ValuesPanel";
import { ABOUT_INTRO, ABOUT_SECTIONS } from "@/data/about";

export default function AboutPage() {
    return (
        <div className="mx-auto max-w-[1280px] px-6 py-8">
            <Breadcrumb items={[{ label: "Nosotros" }]} />

            <Card className="mt-4 p-6 md:p-10">
                <div className="grid gap-10 lg:grid-cols-3">
                    {/* Columna izquierda: quiénes somos */}
                    <div>
                        <PageTitle>¿Quiénes somos?</PageTitle>
                        <p className="mt-3 text-gray-700">{ABOUT_INTRO}</p>

                        <div className="mt-8 space-y-6">
                            {ABOUT_SECTIONS.map((section) => (
                                <InfoItem key={section.id} icon={section.icon} title={section.title}>
                                    {section.text}
                                </InfoItem>
                            ))}
                        </div>
                    </div>

                    {/* Columna central: ilustración (solo en pantallas grandes) */}
                    <div className="hidden items-center justify-center lg:flex" aria-hidden="true">
                        {/* Cuando tengas la ilustración, cámbiala por <img src={edificio} alt="" /> */}
                        <Building2 size={240} strokeWidth={1} className="text-brand-dark" />
                    </div>

                    {/* Columna derecha: valores */}
                    <ValuesPanel />
                </div>
            </Card>
        </div>
    );
}