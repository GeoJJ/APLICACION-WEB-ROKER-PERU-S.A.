import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Building2, CalendarDays, ClipboardCheck, Clock3, Search, Users } from 'lucide-react';
import Breadcrumb from '@/components/shared/Breadcrumb';
import PageTitle from '@/components/shared/PageTitle';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import { useCatalog } from '@/hooks/useCatalog';
import placeholder from '@/assets/service-placeholder.svg';

const keyOf = value => String(value || '').normalize('NFD').replace(/\p{Diacritic}/gu, '').trim().toLocaleLowerCase('es');
const labelOf = value => {
    const text = String(value).trim().toLocaleLowerCase('es');
    return text.charAt(0).toLocaleUpperCase('es') + text.slice(1);
};
const withUnit = (value, singular, plural, fallback) => {
    const text = String(value ?? '').trim();
    if (!text) return fallback;
    return /^\d+$/.test(text) ? `${text} ${Number(text) === 1 ? singular : plural}` : text;
};

export default function ServicesPage() {
    const { items, loading, error } = useCatalog('services');
    const [search, setSearch] = useState('');
    const [category, setCategory] = useState('');
    const categories = [...new Map(items.filter(s => s.category).map(s => [keyOf(s.category), labelOf(s.category)])).entries()].sort((a, b) => a[1].localeCompare(b[1], 'es'));
    const filtered = items.filter(s => (!category || keyOf(s.category) === category) && keyOf(`${s.name} ${s.description || ''} ${s.category || ''}`).includes(keyOf(search)));

    return <div className="min-h-screen bg-gray-50 pb-16">
        <div className="mx-auto max-w-[1280px] px-4 py-8 md:px-6">
            <Breadcrumb items={[{ label: 'Servicios' }]} />
            <PageTitle as="h1" className="mb-3 mt-5">Nuestros servicios</PageTitle>
            <p className="text-sm leading-7 text-gray-500">Encuentra el servicio que necesitas y cuéntanos dónde y cuándo lo necesitas. Nuestro equipo te ayudará a coordinar la atención.</p>
            <div className="my-7 flex items-start gap-3 rounded-xl border border-blue-100 bg-blue-50/70 px-5 py-4 text-brand">
                <ClipboardCheck size={22} className="mt-0.5 shrink-0" aria-hidden="true" />
                <p className="text-sm leading-6"><span className="font-semibold">Solicita en pocos pasos.</span> Selecciona un servicio y completa los datos del ambiente y horario. Un vendedor revisará tu solicitud y coordinará la atención.</p>
            </div>
            <div className="grid gap-4 rounded-2xl border border-gray-200 bg-white p-5 shadow-sm sm:grid-cols-[minmax(0,1fr)_minmax(240px,0.55fr)]">
                <div><label htmlFor="service-search" className="mb-2 block text-sm font-medium text-gray-700">Buscar servicio</label>
                    <div className="relative"><input id="service-search" placeholder="Nombre, descripción o categoría..." className="bo-input pr-11" value={search} onChange={e => setSearch(e.target.value)} /><Search size={20} className="pointer-events-none absolute right-3 top-3 text-brand" aria-hidden="true" /></div>
                </div>
                <div><label id="service-category-label" className="mb-2 block text-sm font-medium text-gray-700">Categorías</label>
                    <Select id="service-category" aria-label="Categorías" aria-labelledby="service-category-label" value={category} onChange={e => setCategory(e.target.value)} options={[{ value: '', label: 'Todas las categorías' }, ...categories.map(([value, label]) => ({ value, label }))]} />
                </div>
            </div>
            {error && <p role="alert" className="mt-5 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}
            <p aria-live="polite" className="my-5 text-sm text-gray-500">{loading ? 'Cargando servicios...' : `${filtered.length} ${filtered.length === 1 ? 'servicio disponible' : 'servicios disponibles'}`}</p>
            {loading ? <div className="rounded-2xl border border-gray-200 bg-white p-10 text-center text-gray-500">Estamos consultando nuestros servicios.</div> : filtered.length ?
                <ul className="space-y-5" aria-label="Lista de servicios">{filtered.map(service => <li key={service.id}>
                    <article className="group overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition duration-200 hover:border-blue-200 hover:shadow-md sm:flex">
                        <div className="relative flex shrink-0 items-center justify-center border-b border-blue-100 bg-blue-50/60 p-5 sm:w-48 sm:border-b-0 sm:border-r lg:w-56"><img src={placeholder} alt="Imagen referencial del servicio" className="h-32 w-44 object-contain sm:h-40" /></div>
                        <div className="flex min-w-0 flex-1 flex-col gap-5 p-5 md:flex-row md:items-center md:gap-7 lg:p-7">
                            <div className="min-w-0 flex-1">
                                {service.category && <span className="mb-3 inline-flex rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-brand">{labelOf(service.category)}</span>}
                                <h2 className="text-lg font-semibold leading-7 text-gray-900 lg:text-xl">{service.name}</h2>
                                <p className="mt-2 text-sm leading-7 text-gray-500">{service.description || 'Contáctanos para conocer el alcance y las características de este servicio.'}</p>
                                <dl className="mt-4 flex flex-wrap gap-x-6 gap-y-3 text-sm text-gray-600">
                                    <div className="flex items-center gap-2"><Clock3 size={17} className="shrink-0 text-brand" aria-hidden="true" /><dt className="sr-only">Duración estimada</dt><dd>{withUnit(service.duration, 'hora', 'horas', 'Duración por coordinar')}</dd></div>
                                    <div className="flex items-center gap-2"><Users size={17} className="shrink-0 text-brand" aria-hidden="true" /><dt className="sr-only">Personal requerido</dt><dd>{withUnit(service.personnel, 'persona', 'personas', 'Personal por coordinar')}</dd></div>
                                </dl>
                            </div>
                            <Button as={Link} to={`/servicios/${encodeURIComponent(service.id)}/solicitar`} size="sm" className="w-full gap-2 self-start md:w-auto md:shrink-0 md:self-center" aria-label={`Solicitar ${service.name}`}><CalendarDays size={17} aria-hidden="true" />Solicitar<ArrowRight size={16} aria-hidden="true" /></Button>
                        </div>
                    </article>
                </li>)}</ul> :
                <div className="rounded-2xl border border-dashed border-gray-300 bg-white px-6 py-12 text-center"><Search size={28} className="mx-auto mb-3 text-brand" aria-hidden="true" /><h2 className="font-semibold text-gray-900">No encontramos servicios para estos filtros</h2><p className="mt-2 text-sm text-gray-500">Prueba con otro nombre o selecciona todas las categorías.</p><button type="button" onClick={() => { setSearch(''); setCategory(''); }} className="mt-4 cursor-pointer text-sm font-semibold text-brand underline underline-offset-4">Limpiar filtros</button></div>}
            <section aria-labelledby="service-contract-title" className="mt-10 rounded-2xl border border-blue-100 bg-blue-50 p-6 md:flex md:items-center md:gap-8 lg:p-8">
                <div className="mb-4 flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-white text-brand shadow-sm md:mb-0"><Building2 size={26} aria-hidden="true" /></div>
                <div className="min-w-0 flex-1"><h2 id="service-contract-title" className="text-lg font-semibold leading-7 text-gray-900">¿Necesitas un servicio de mayor alcance o un contrato por un periodo definido?</h2><p className="mt-2 text-sm leading-7 text-gray-600">Contáctanos para coordinar servicios para instalaciones de mayor tamaño, atención recurrente o contratos según las necesidades de tu organización.</p></div>
                <Button as={Link} to="/contacto?motivo=contrato" size="sm" className="mt-5 w-full gap-2 md:mt-0 md:w-auto md:shrink-0">Contactar con la empresa<ArrowRight size={17} aria-hidden="true" /></Button>
            </section>
        </div>
    </div>;
}
