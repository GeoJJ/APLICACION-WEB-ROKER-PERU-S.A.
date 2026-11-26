import { Link } from 'react-router-dom';
import { ArrowRight, Award, FlaskConical, ShieldCheck, Sparkles } from 'lucide-react';
import Breadcrumb from '@/components/shared/Breadcrumb';
import InfoItem from '@/components/shared/InfoItem';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import ValuesPanel from '@/components/features/about/ValuesPanel';
import CertificationsSection from '@/components/features/about/CertificationsSection';
import { ABOUT_INTRO, ABOUT_SECTIONS } from '@/data/about';
import laboratory from '@/assets/about/laboratorio_bioseguridad_roker.webp';

const pillars = [
    { icon: ShieldCheck, title: 'Bioseguridad', text: 'Soluciones orientadas al cuidado de las personas y a la protección de los espacios que compartimos.' },
    { icon: Award, title: 'Calidad', text: 'Procesos y productos respaldados por estándares de gestión y una cultura de mejora continua.' },
    { icon: FlaskConical, title: 'Innovación', text: 'Experiencia y desarrollo tecnológico para responder a las necesidades de higiene y desinfección.' },
];

export default function AboutPage() {
    return <div className="mx-auto max-w-[1280px] px-4 py-8 md:px-6">
        <Breadcrumb items={[{ label: 'Nosotros' }]} />
        <section aria-labelledby="about-title" className="mt-6 grid overflow-hidden rounded-2xl border border-blue-100 bg-blue-50/60 shadow-sm lg:grid-cols-2">
            <div className="flex flex-col justify-center p-6 md:p-10 lg:p-12">
                <p className="flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.15em] text-brand"><Sparkles size={18} aria-hidden="true" />Somos Roker Perú</p>
                <h1 id="about-title" className="mt-5 text-3xl font-semibold leading-tight text-gray-900 md:text-4xl">La bioseguridad comienza con la confianza</h1>
                <p className="mt-5 text-sm leading-7 text-gray-600">{ABOUT_INTRO}</p>
                <Button as={Link} to="/servicios" className="mt-7 gap-2 self-start" size="sm">Conocer nuestros servicios<ArrowRight size={17} aria-hidden="true" /></Button>
            </div>
            <div className="relative bg-blue-100">
                <img src={laboratory} alt="Profesional de laboratorio utilizando un microscopio junto a un producto de Roker" width="650" height="650" className="h-full max-h-[520px] min-h-72 w-full object-cover object-center" />
                <div className="absolute bottom-5 left-5 right-5 rounded-xl border border-white/70 bg-white/95 p-4 shadow-sm"><p className="text-sm font-semibold text-brand">Experiencia al servicio del cuidado</p><p className="mt-1 text-xs leading-5 text-gray-600">Higiene, desinfección y bioseguridad.</p></div>
            </div>
        </section>

        <section aria-label="Nuestros pilares" className="my-7 grid gap-5 md:grid-cols-3">
            {pillars.map(({ icon: Icon, title, text }) => <article key={title} className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-blue-200 hover:shadow-md motion-reduce:transform-none">
                <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-brand"><Icon size={24} aria-hidden="true" /></div>
                <h2 className="text-lg font-semibold text-gray-900">{title}</h2><p className="mt-2 text-sm leading-7 text-gray-500">{text}</p>
            </article>)}
        </section>

        <Card className="p-6 md:p-10">
            <div className="grid gap-8 lg:grid-cols-[minmax(0,1.6fr)_minmax(280px,1fr)] lg:gap-12">
                <div><p className="text-xs font-semibold uppercase tracking-wider text-brand">Lo que nos mueve</p><h2 className="mt-3 text-2xl font-semibold text-gray-900">Nuestra forma de trabajar</h2>
                    <div className="mt-7 space-y-6">{ABOUT_SECTIONS.map(section => <InfoItem key={section.id} icon={section.icon} title={section.title}>{section.text}</InfoItem>)}</div>
                </div>
                <ValuesPanel />
            </div>
        </Card>

        <section aria-labelledby="about-experience-title" className="mt-7 grid gap-6 rounded-2xl bg-brand p-6 text-white md:grid-cols-[1fr_auto] md:items-center md:p-9">
            <div><p className="text-xs font-medium uppercase tracking-wider text-blue-100">Una trayectoria que nos respalda</p><h2 id="about-experience-title" className="mt-3 text-2xl font-semibold">Más de tres décadas en bioseguridad</h2><p className="mt-3 max-w-2xl text-sm leading-7 text-blue-100">Un camino de experiencia, aprendizaje y compromiso con la calidad.</p></div>
            <div className="border-t border-white/25 pt-5 md:border-l md:border-t-0 md:pl-8 md:pt-0"><p className="text-3xl font-semibold">Desde 2005</p><p className="mt-2 max-w-56 text-sm leading-6 text-blue-100">Buenas Prácticas de Manufactura.</p></div>
        </section>

        <section aria-labelledby="about-explore-title" className="mt-7 flex flex-col gap-5 rounded-2xl border border-blue-100 bg-blue-50/50 p-6 lg:flex-row lg:items-center lg:justify-between">
            <div><h2 id="about-explore-title" className="text-lg font-semibold text-gray-900">Conoce las soluciones de Roker</h2><p className="mt-2 text-sm leading-6 text-gray-500">Explora nuestros productos y encuentra el servicio que necesitas.</p></div>
            <div className="flex flex-col gap-3 sm:flex-row"><Button as={Link} to="/productos" size="sm" className="gap-2">Explorar productos<ArrowRight size={16} aria-hidden="true" /></Button><Button as={Link} to="/servicios" variant="outline" size="sm">Conocer servicios</Button></div>
        </section>
        <CertificationsSection />
    </div>;
}
