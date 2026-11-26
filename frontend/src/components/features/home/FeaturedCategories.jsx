import { Package, ShieldPlus, SprayCan } from "lucide-react";
import PageTitle from "@/components/shared/PageTitle";
import { CATEGORIES } from "@/data/categories";
import { catalogProductCategories } from "@/data/catalogCategories";
import { useCatalog } from "@/hooks/useCatalog";
import CategoryCard from "./CategoryCard";

const icons = { bioseguridad: ShieldPlus, limpieza: SprayCan, epp: ShieldPlus };

export default function FeaturedCategories() {
    const { items, loading, error } = useCatalog("products");
    const categories = catalogProductCategories(items);
    return <section className="mx-auto max-w-[1280px] px-6 py-14">
        <PageTitle as="h2">Categorías destacadas</PageTitle>
        {loading ? <p role="status" className="mt-6 text-gray-500">Cargando categorías...</p>
            : error ? <p role="alert" className="mt-6 text-gray-500">No se pudieron cargar las categorías. Consulta el catálogo de productos.</p>
                : <div className="mt-8 grid grid-cols-2 gap-6 md:grid-cols-3">{categories.map(category => {
                    const original = CATEGORIES.find(item => item.id === category.value);
                    return <CategoryCard key={category.value} name={category.label} image={original?.image}
                        icon={original?.icon || icons[category.value] || Package}
                        to={`/productos?categoria=${encodeURIComponent(category.value)}`} />;
                })}</div>}
    </section>;
}
