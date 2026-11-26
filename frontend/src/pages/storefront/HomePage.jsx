import { Link } from "react-router-dom";
import { ArrowRight, Building2, CheckCircle2, Clock3, Hospital, House, ShieldCheck, UtensilsCrossed } from "lucide-react";
import Button from "@/components/ui/Button";
import ProductsCarousel from "@/components/features/home/ProductsCarousel";
import FeatureBar from "@/components/features/home/FeatureBar";
import { useCatalog } from "@/hooks/useCatalog";
import { catalogCategoryKey, catalogProductCategories } from "@/data/catalogCategories";
import institution from "@/assets/home/imagen_categoria_bioseguridad.webp";
import placeholder from "@/assets/service-placeholder.svg";
import protection from "@/assets/home/imagen_categoria_epp.jpg";
import cleaning from "@/assets/home/imagen_categoria_limpieza.jpg";

const CATEGORY_IMAGES = { bioseguridad: institution, epp: protection, limpieza: cleaning };

const SECTORS = [
    { title: "Salud y consultorios", text: "Insumos de bioseguridad y atención para tus ambientes de trabajo.", icon: Hospital },
    { title: "Oficinas y empresas", text: "Productos y servicios para el cuidado diario de tus instalaciones.", icon: Building2 },
    { title: "Restaurantes y negocios", text: "Soluciones de limpieza para los espacios donde atiendes a tus clientes.", icon: UtensilsCrossed },
    { title: "Hogares", text: "Todo para mantener tus espacios limpios, cuidados y protegidos.", icon: House },
];

function imageFallback(event) {
    event.currentTarget.onerror = null;
    event.currentTarget.src = placeholder;
}

function SectionHeading({ eyebrow, title, description, link, to }) {
    return <div className="mb-8 flex flex-wrap items-end justify-between gap-5">
        <div className="max-w-2xl"><p className="text-xs font-semibold uppercase tracking-[0.18em] text-brand">{eyebrow}</p><h2 className="mt-3 text-2xl font-semibold leading-tight text-gray-900 md:text-3xl">{title}</h2>{description && <p className="mt-3 text-sm leading-7 text-gray-500">{description}</p>}</div>
        {link && <Link to={to} className="inline-flex items-center gap-2 text-sm font-semibold text-brand hover:underline">{link}<ArrowRight size={17} aria-hidden="true" /></Link>}
    </div>;
}

export default function HomePage() {
    const products = useCatalog("products");
    const services = useCatalog("services");
    const categories = catalogProductCategories(products.items);
    const featuredServices = services.items.slice(0, 3);

    return <div className="bg-white">
        <section className="overflow-hidden bg-brand-dark">
            <div className="mx-auto grid max-w-[1440px] lg:min-h-[530px] lg:grid-cols-2">
                <div className="flex flex-col justify-center px-6 py-12 md:px-10 lg:py-16 xl:pl-20">
                    <p className="mb-5 flex items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-blue-100"><ShieldCheck size={18} aria-hidden="true" />Bioseguridad para cada espacio</p>
                    <h1 className="max-w-xl text-4xl font-semibold leading-tight text-white md:text-5xl lg:text-6xl">Cuidamos tus espacios.<br /><span className="text-blue-200">Protegemos lo que importa.</span></h1>
                    <p className="mt-6 max-w-lg text-base leading-8 text-blue-50">Productos de bioseguridad y servicios de limpieza y desinfección para tu hogar, tu empresa y las personas que te rodean.</p>
                    <div className="mt-8 flex flex-wrap gap-3"><Button as={Link} to="/productos" variant="secondary" className="gap-2">Explorar productos<ArrowRight size={18} aria-hidden="true" /></Button><Button as={Link} to="/servicios" className="border border-white/40 bg-transparent shadow-none hover:bg-white/10">Solicitar un servicio</Button></div>
                    <p className="mt-7 flex items-center gap-2 text-sm text-blue-100"><CheckCircle2 size={17} aria-hidden="true" />Productos y atención en un solo lugar</p>
                </div>
                <div className="relative min-h-72 lg:min-h-full"><img src={institution} alt="Profesional de laboratorio de Roker utilizando un microscopio" className="absolute inset-0 h-full w-full object-cover object-center" fetchPriority="high" /></div>
            </div>
        </section>
        <FeatureBar />
        <section className="mx-auto max-w-[1280px] px-6 py-12 md:py-16">
            <SectionHeading eyebrow="Encuentra lo que necesitas" title="Explora nuestras categorías" description="Selecciona una categoría y encuentra los productos disponibles para el cuidado de tus espacios." link="Ver todo el catálogo" to="/productos" />
            {products.loading ? <p role="status" className="text-gray-500">Cargando categorías...</p> : products.error ? <p role="alert" className="text-red-700">{products.error}</p> : categories.length ? <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{categories.map(category => {
                const image = CATEGORY_IMAGES[category.value] || products.items.find(item => catalogCategoryKey(item.category) === category.value)?.image || institution;
                return <Link key={category.value} to={`/productos?categoria=${encodeURIComponent(category.value)}`} className="group relative isolate block h-72 overflow-hidden rounded-2xl bg-brand-dark shadow-sm transition hover:-translate-y-1 hover:shadow-lg"><img src={image} onError={imageFallback} alt={`Categoría ${category.label}`} loading="lazy" className="absolute inset-0 -z-10 h-full w-full object-cover transition duration-500 motion-safe:group-hover:scale-105" /><div className="absolute inset-0 -z-10 bg-gradient-to-t from-slate-950/90 via-slate-950/30 to-transparent" /><div className="flex h-full items-end justify-between gap-4 p-6 text-white"><div><p className="mb-2 text-xs font-medium uppercase tracking-[0.16em] text-blue-100">Explora la categoría</p><h3 className="text-2xl font-semibold">{category.label}</h3><p className="mt-2 text-sm text-blue-50">{category.count} {category.count === 1 ? "producto disponible" : "productos disponibles"}</p></div><span className="grid h-10 w-10 shrink-0 place-items-center rounded-full border border-white/40 bg-white/10 transition group-hover:bg-white group-hover:text-brand"><ArrowRight size={21} aria-hidden="true" /></span></div></Link>;
            })}</div> : <p className="text-gray-500">Pronto encontrarás nuestras categorías disponibles.</p>}
        </section>
        <section className="border-y border-gray-100 bg-gray-50">
            <div className="mx-auto max-w-[1280px] px-6 py-12 md:py-16"><SectionHeading eyebrow="Nuestro catálogo" title="Productos destacados" description="Explora nuestra selección por categoría y agrega lo que necesitas a tu carrito." link="Explorar productos" to="/productos" />
                {products.loading ? <p role="status" className="text-gray-500">Cargando productos...</p> : products.error ? <p role="alert" className="text-red-700">{products.error}</p> : <ProductsCarousel products={products.items} />}

            </div>
        </section>
        <section className="mx-auto max-w-[1280px] px-6 py-12 md:py-16"><SectionHeading eyebrow="Soluciones para tu entorno" title="Cada espacio tiene una necesidad" description="Conversemos sobre los productos y servicios que requiere tu ambiente." />
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">{SECTORS.map(({ title, text, icon: Icon }) => <Link key={title} to="/contacto?motivo=servicio" className="group rounded-2xl border border-blue-100 bg-blue-50/60 p-6 transition hover:border-blue-200 hover:bg-blue-50"><span className="mb-5 inline-flex rounded-xl bg-white p-3 text-brand shadow-sm"><Icon size={27} aria-hidden="true" /></span><h3 className="text-lg font-semibold text-gray-900">{title}</h3><p className="mt-3 text-sm leading-7 text-gray-600">{text}</p><span className="mt-5 inline-flex items-center gap-2 text-sm font-semibold text-brand">Consultar<ArrowRight size={16} aria-hidden="true" /></span></Link>)}</div>
        </section>
        <section className="bg-gray-50"><div className="mx-auto max-w-[1280px] px-6 py-12 md:py-16"><SectionHeading eyebrow="Un equipo para ayudarte" title="Servicios que se adaptan a tus espacios" description="Elige el servicio e indícanos dónde y cuándo lo necesitas para coordinar la atención." link="Ver todos los servicios" to="/servicios" />
            {services.loading ? <p role="status" className="text-gray-500">Cargando servicios...</p> : services.error ? <p role="alert" className="text-red-700">{services.error}</p> : featuredServices.length ? <div className="grid gap-6 md:grid-cols-3">{featuredServices.map(service => <article key={service.id} className="flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm"><img src={service.image || placeholder} onError={imageFallback} alt={service.name} loading="lazy" className="h-48 w-full object-cover" /><div className="flex flex-1 flex-col p-6"><h3 className="text-lg font-semibold leading-7 text-gray-900">{service.name}</h3><p className="mt-3 text-sm leading-7 text-gray-500">{service.description}</p>{service.duration && <p className="mt-4 flex items-center gap-2 text-sm text-gray-600"><Clock3 size={16} className="text-brand" aria-hidden="true" />Duración estimada: {service.duration}{/^\d+$/.test(String(service.duration)) ? " horas" : ""}</p>}<Button as={Link} to={`/servicios/${encodeURIComponent(service.id)}/solicitar`} size="sm" className="mt-6 gap-2 self-start">Solicitar servicio<ArrowRight size={16} aria-hidden="true" /></Button></div></article>)}</div> : <p className="text-gray-500">No hay servicios disponibles por el momento.</p>}
        </div></section>
        <section className="mx-auto max-w-[1280px] px-6 pb-14"><div className="rounded-3xl bg-brand-dark p-7 text-white md:flex md:items-center md:justify-between md:gap-10 md:p-12"><div className="max-w-2xl"><p className="text-xs font-semibold uppercase tracking-[0.18em] text-blue-200">Atención para tu organización</p><h2 className="mt-3 text-2xl font-semibold leading-tight md:text-3xl">Un aliado para cuidar tu empresa</h2><p className="mt-4 text-sm leading-7 text-blue-50">¿Necesitas abastecimiento recurrente, un servicio de mayor alcance o un contrato por un periodo definido? Cuéntanos qué necesitas y coordinemos una solución.</p></div><Button as={Link} to="/contacto?motivo=contrato" variant="secondary" className="mt-6 shrink-0 gap-2 md:mt-0">Contactar con la empresa<ArrowRight size={18} aria-hidden="true" /></Button></div></section>
    </div>;
}
