import { CATEGORIES } from "@/data/categories";

// Productos: son las mismas del inicio y del catálogo de la tienda (data/categories.js), así hay una sola lista.
export const PRODUCT_CATEGORIES = CATEGORIES.map(({ id, name }) => ({ value: id, label: name }));

// Servicios: para agregar o quitar una categoría basta con editar esta lista.
export const SERVICE_CATEGORIES = [
    { value: "limpieza", label: "Limpieza" },
    { value: "desinfeccion", label: "Desinfección" },
    { value: "industrial", label: "Servicio industrial" },
    { value: "otros", label: "Otros servicios" },
];

const normalize = value => String(value ?? "").trim().toLowerCase();

export function categoryValue(list, stored) {
    const found = list.find(option => option.value === normalize(stored));
    return found ? found.value : String(stored ?? "").trim();
}

export function categoryLabel(list, stored) {
    const found = list.find(option => option.value === normalize(stored));
    return found ? found.label : stored || "-";
}
export const catalogCategoryKey = value => String(value ?? "").normalize("NFD")
    .replace(/\p{Diacritic}/gu, "").trim().replace(/\s+/g, " ").toLocaleLowerCase("es");

export function catalogProductCategories(products) {
    const categories = new Map();
    for (const product of products) {
        const value = catalogCategoryKey(product.category);
        if (!value) continue;
        const label = value === "epp" ? "EPP" : value.charAt(0).toLocaleUpperCase("es") + value.slice(1);
        const current = categories.get(value);
        categories.set(value, { value, label, count: (current?.count || 0) + 1 });
    }
    return [...categories.values()].sort((a, b) => a.label.localeCompare(b.label, "es"));
}
