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