import { useState, useMemo, useEffect } from "react";
import { Search, ChevronDown } from "lucide-react";
import Breadcrumb from "@/components/shared/Breadcrumb";
import PageTitle from "@/components/shared/PageTitle";
import RangeSlider from "@/components/ui/RangeSlider";
import ProductCard from "@/components/features/catalog/ProductCard";
import { obtenerProductos } from "@/services/productService";

export default function CatalogPage() {
    // Estados para los productos del backend
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Estados de filtros y controles de UI
    const [search, setSearch] = useState("");
    const [sort, setSort] = useState("");
    const [priceRange, setPriceRange] = useState([0, 200]);
    const [category, setCategory] = useState("");


    useEffect(() => {
        const cargarDatos = async () => {
            try {
                setLoading(true);
                const data = await obtenerProductos();
                setProducts(data);
                setError(null);
            } catch (err) {
                setError(err.message || "No se pudo obtener la lista de productos del servidor.");
            } finally {
                setLoading(false);
            }
        };

        cargarDatos();
    }, []);

    const filteredProducts = useMemo(() => {
        let result = products;

        // Filtro por nombre
        if (search) {
            result = result.filter((p) => {
                const productName = p.nombreProducto || p.name || p.nombre || "";
                return productName.toLowerCase().includes(search.toLowerCase());
            });
        }

        // Filtro por categoría
        if (category) {
            result = result.filter((p) => {
                const productCategory = p.categoriaProducto || p.category || p.categoria || "";
                return productCategory.toLowerCase().includes(category.toLowerCase());
            });
        }

        // Filtro por rango de precio
        result = result.filter((p) => {
            const price = p.precioProducto ?? p.price ?? p.precio ?? 0;
            return price >= priceRange[0] && price <= priceRange[1];
        });

        // Ordenamiento por precio
        if (sort === "precio-asc") {
            result = [...result].sort((a, b) => {
                const priceA = a.precioProducto ?? a.price ?? a.precio ?? 0;
                const priceB = b.precioProducto ?? b.price ?? b.precio ?? 0;
                return priceA - priceB;
            });
        } else if (sort === "precio-desc") {
            result = [...result].sort((a, b) => {
                const priceA = a.precioProducto ?? a.price ?? a.precio ?? 0;
                const priceB = b.precioProducto ?? b.price ?? b.precio ?? 0;
                return priceB - priceA;
            });
        }

        return result;
    }, [products, search, category, sort, priceRange]);

    return (
        <main className="bg-gray-50 min-h-screen pb-20">
            <div className="mx-auto max-w-[1280px] px-6 py-8">
                {/* Breadcrumb */}
                <Breadcrumb items={[{ label: "Productos" }]} />

                {/* Título */}
                <PageTitle as="h1" className="mb-8">Categorías</PageTitle>

                <div className="grid gap-6 lg:grid-cols-4">
                    {/* Sidebar de Filtros */}
                    <aside className="lg:col-span-1">
                        <div className="rounded-2xl border-2 border-gray-100 bg-white p-6 shadow-sm">
                            <h3 className="mb-4 text-base font-semibold text-gray-900">
                                Tipos de Categorías
                            </h3>

                            {/* Dropdown de categorías */}
                            <div className="mb-6">
                                <div className="relative">
                                    <select
                                        value={category}
                                        onChange={(e) => setCategory(e.target.value)}
                                        className="w-full cursor-pointer appearance-none rounded-xl border-2 border-brand bg-white px-4 py-3 pr-10 font-medium text-brand outline-none focus:border-brand-dark"
                                    >
                                        <option value="">Todas las categorías</option>
                                        <option value="mascarillas">Mascarillas</option>
                                        <option value="guantes">Guantes</option>
                                        <option value="desinfectantes">Desinfectantes</option>
                                        <option value="trajes">Trajes de protección</option>
                                        <option value="accesorios">Accesorios</option>
                                    </select>
                                    <ChevronDown
                                        size={20}
                                        className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-brand"
                                    />
                                </div>
                            </div>

                            {/* Precio */}
                            <h3 className="mb-4 text-base font-semibold text-gray-900">
                                Precio
                            </h3>
                            <RangeSlider
                                min={0}
                                max={200}
                                step={5}
                                value={priceRange}
                                onChange={setPriceRange}
                            />
                            <div className="mt-2 flex justify-between text-sm font-medium text-gray-600">
                                <span>S/.{priceRange[0]}</span>
                                <span>S/.{priceRange[1]}</span>
                            </div>
                        </div>
                    </aside>

                    {/* Contenido Principal */}
                    <div className="lg:col-span-3">
                        {/* Barra de búsqueda y ordenamiento */}
                        <div className="mb-6 grid gap-4 md:grid-cols-2">
                            {/* Buscador */}
                            <div className="relative">
                                <input
                                    type="text"
                                    placeholder="Buscar un producto"
                                    value={search}
                                    onChange={(e) => setSearch(e.target.value)}
                                    className="w-full rounded-xl border-2 border-gray-200 bg-white px-4 py-3 pl-4 pr-12 text-gray-700 placeholder:text-gray-400 outline-none focus:border-brand"
                                />
                                <Search
                                    size={22}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-brand"
                                />
                            </div>

                            {/* Ordenar por */}
                            <div className="relative">
                                <select
                                    value={sort}
                                    onChange={(e) => setSort(e.target.value)}
                                    className="w-full cursor-pointer appearance-none rounded-xl border-2 border-gray-200 bg-white px-4 py-3 pr-10 text-gray-700 outline-none focus:border-brand"
                                >
                                    <option value="">Ordenar por</option>
                                    <option value="precio-asc">Precio: Menor a Mayor</option>
                                    <option value="precio-desc">Precio: Mayor a Menor</option>
                                </select>
                                <ChevronDown
                                    size={20}
                                    className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-brand"
                                />
                            </div>
                        </div>

                        {/* Renderizado Condicional: Carga, Error o Productos */}
                        {loading ? (
                            <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white py-20 text-center">
                                <p className="text-lg font-semibold text-gray-700">Cargando catálogo...</p>
                                <p className="text-sm text-gray-500">Obteniendo productos desde el servidor</p>
                            </div>
                        ) : error ? (
                            <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-red-200 bg-white py-20 text-center">
                                <p className="text-lg font-semibold text-red-600">Error al conectar con la base de datos</p>
                                <p className="text-sm text-gray-500">{error}</p>
                            </div>
                        ) : filteredProducts.length > 0 ? (
                            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                                {filteredProducts.map((product) => (
                                    <ProductCard key={product.id || product.idProducto} product={product} />
                                ))}
                            </div>
                        ) : (
                            <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-white py-20 text-center">
                                <p className="text-lg font-semibold text-gray-700">
                                    No encontramos productos
                                </p>
                                <p className="text-gray-500">
                                    Intenta ajustar los filtros de búsqueda
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </main>
    );
}
