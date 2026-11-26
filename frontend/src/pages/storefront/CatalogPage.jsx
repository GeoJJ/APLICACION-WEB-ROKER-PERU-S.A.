import { useState, useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ArrowUpRight, Building2, MessageCircle, Search } from "lucide-react";
import Breadcrumb from "@/components/shared/Breadcrumb";
import PageTitle from "@/components/shared/PageTitle";
import RangeSlider from "@/components/ui/RangeSlider";
import Select from "@/components/ui/Select";
import ProductCard from "@/components/features/catalog/ProductCard";
import { useCatalog } from "@/hooks/useCatalog";
import { catalogCategoryKey, catalogProductCategories } from "@/data/catalogCategories";

export default function CatalogPage() {
    const { items: products, loading, error } = useCatalog("products");
    const [params, setParams] = useSearchParams();
    const [search, setSearch] = useState("");
    const [sort, setSort] = useState("");
    const [priceRange, setPriceRange] = useState([0, null]);
    const category = catalogCategoryKey(params.get("categoria"));
    const categories = useMemo(() => catalogProductCategories(products), [products]);
    const priceLimit = Math.max(200, ...products.map(p => Math.ceil((Number(p.price) || 0) / 5) * 5));
    const minimum = Math.min(priceRange[0], priceLimit);
    const maximum = Math.min(priceRange[1] ?? priceLimit, priceLimit);
    const filteredProducts = useMemo(() => {
        const result = products.filter(product =>
            (!category || catalogCategoryKey(product.category) === category)
            && catalogCategoryKey(product.name).includes(catalogCategoryKey(search))
            && Number(product.price) >= minimum && Number(product.price) <= maximum);
        if (sort === "precio-asc") return result.sort((a, b) => a.price - b.price);
        if (sort === "precio-desc") return result.sort((a, b) => b.price - a.price);
        return result;
    }, [products, category, search, minimum, maximum, sort]);

    function selectCategory(value) {
        setParams(current => {
            const next = new URLSearchParams(current);
            if (value) next.set("categoria", value);
            else next.delete("categoria");
            return next;
        });
    }

    function resetFilters() {
        selectCategory("");
        setSearch("");
        setSort("");
        setPriceRange([0, null]);
    }

    return <main className="min-h-screen bg-gray-50 pb-20">
        <div className="mx-auto max-w-[1280px] px-6 py-8">
            <Breadcrumb items={[{ label: "Productos" }]} />
            <PageTitle as="h1" className="mb-8 mt-5">Productos</PageTitle>
            {error && <p role="alert" className="mb-4 rounded-xl bg-red-50 p-4 text-red-700">{error}</p>}
            <div className="grid gap-6 lg:grid-cols-4">
                <aside className="space-y-4 lg:sticky lg:top-24 lg:col-span-1 lg:max-h-[calc(100vh-7rem)] lg:self-start lg:overflow-y-auto lg:pr-1" aria-label="Filtros y asesoría de productos">
                    <div className="rounded-2xl border-2 border-gray-100 bg-white p-6 shadow-sm">
                        <label id="product-category-label" className="mb-4 block text-base font-semibold text-gray-900">Tipos de categorías</label>
                        <Select id="product-category" aria-labelledby="product-category-label" value={category}
                            onChange={event => selectCategory(event.target.value)}
                            options={[{ value: "", label: "Todas las categorías" }, ...categories.map(c => ({ value: c.value, label: `${c.label} (${c.count})` }))]} />
                        <h2 className="mb-4 mt-6 text-base font-semibold text-gray-900">Precio</h2>
                        <RangeSlider min={0} max={priceLimit} step={5} value={[minimum, maximum]} onChange={setPriceRange} />
                        <button type="button" onClick={resetFilters} className="mt-6 cursor-pointer text-sm font-semibold text-brand underline underline-offset-4">Limpiar filtros</button>
                    </div>
                    <section className="rounded-2xl border border-blue-100 bg-blue-50 p-5" aria-labelledby="catalog-help-title">
                        <MessageCircle size={24} className="mb-3 text-brand" aria-hidden="true" />
                        <h2 id="catalog-help-title" className="text-base font-semibold text-gray-900">¿Necesitas ayuda para elegir?</h2>
                        <p className="mt-2 text-sm leading-6 text-gray-600">Cuéntanos qué espacio quieres cuidar y te orientamos sobre nuestros productos.</p>
                        <Link to="/contacto?motivo=general" className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-brand underline-offset-4 hover:underline">Recibir asesoría<ArrowUpRight size={16} aria-hidden="true" /></Link>
                    </section>
                    <section className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm" aria-labelledby="catalog-business-title">
                        <Building2 size={24} className="mb-3 text-brand" aria-hidden="true" />
                        <h2 id="catalog-business-title" className="text-base font-semibold text-gray-900">Compras para tu empresa</h2>
                        <p className="mt-2 text-sm leading-6 text-gray-600">Coordina pedidos de mayor volumen o el abastecimiento recurrente de tu organización.</p>
                        <Link to="/contacto?motivo=general" className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-brand underline-offset-4 hover:underline">Contactar con la empresa<ArrowUpRight size={16} aria-hidden="true" /></Link>
                    </section>
                </aside>
                <div className="lg:col-span-3">
                    <div className="mb-4 grid gap-4 md:grid-cols-2">
                        <div className="relative">
                            <input type="search" aria-label="Buscar un producto" placeholder="Buscar un producto" value={search} onChange={event => setSearch(event.target.value)} className="w-full rounded-xl border-2 border-gray-200 bg-white py-3 pl-4 pr-12 text-gray-700 placeholder:text-gray-400 outline-none focus:border-brand" />
                            <Search size={22} className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-brand" aria-hidden="true" />
                        </div>
                        <Select aria-label="Ordenar productos" value={sort} onChange={event => setSort(event.target.value)} options={[{ value: "", label: "Ordenar por" }, { value: "precio-asc", label: "Precio: Menor a Mayor" }, { value: "precio-desc", label: "Precio: Mayor a Menor" }]} />
                    </div>
                    <p role="status" className="mb-5 text-sm text-gray-500">{loading ? "Cargando catálogo..." : `${filteredProducts.length} ${filteredProducts.length === 1 ? "producto disponible" : "productos disponibles"}`}</p>
                    {loading ? <div className="rounded-2xl border border-gray-200 bg-white p-10 text-center text-gray-500">Estamos consultando nuestros productos.</div>
                        : filteredProducts.length ? <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">{filteredProducts.map(product => <ProductCard key={product.id} product={product} />)}</div>
                            : <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white px-5 py-16 text-center"><p className="text-lg font-semibold text-gray-700">No encontramos productos</p><p className="mt-2 text-gray-500">Intenta ajustar los filtros de búsqueda.</p><button type="button" onClick={resetFilters} className="mt-4 cursor-pointer text-sm font-semibold text-brand underline underline-offset-4">Ver todos los productos</button></div>}
                </div>
            </div>
        </div>
    </main>;
}
