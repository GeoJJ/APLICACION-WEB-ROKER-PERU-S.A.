import { ShoppingCart } from "lucide-react";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";

export default function ProductCard({ product }) {
    // Mapeo adaptado exactamente a la respuesta de tu backend en Spring Boot
    const name = product.nombreProducto || product.name || product.nombre || "Nombre del Producto";
    const description = product.descripcionProducto || product.description || product.descripcion || "Sin descripción";
    const image = product.imagenProducto || product.image || product.imagen || "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?w=500&auto=format&fit=crop&q=60";
    const price = product.precioProducto ?? product.price ?? product.precio ?? 0;
    const stock = product.stockProducto ?? product.stock ?? 0;

    let stockBadge = null;
    if (stock === 0) {
        stockBadge = <Badge variant="danger" className="text-xs px-2 py-0.5">Agotado</Badge>;
    } else if (stock < 10) {
        stockBadge = <Badge variant="warning" className="text-xs px-2 py-0.5">Stock bajo</Badge>;
    }

    return (
        <div className="group flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            {/* Imagen del producto */}
            <div className="relative aspect-square overflow-hidden bg-gray-100 p-3">
                <img
                    src={image}
                    alt={name}
                    className="h-full w-full object-contain transition duration-300 group-hover:scale-105"
                />
                {stockBadge && (
                    <div className="absolute left-2 top-2">{stockBadge}</div>
                )}
            </div>

            {/* Información del producto */}
            <div className="flex flex-col gap-2 p-4">
                <div>
                    <h3 className="text-sm font-semibold text-gray-900 line-clamp-2">
                        {name}
                    </h3>
                    <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">
                        {description}
                    </p>
                </div>

                <div className="mt-auto pt-2">
                    <p className="text-lg font-bold text-brand">S/.{Number(price).toFixed(2)}</p>
                    <Button
                        size="sm"
                        className="mt-2 w-full text-sm"
                        disabled={stock === 0}
                    >
                        <ShoppingCart size={16} className="mr-1.5" />
                        Agregar
                    </Button>
                </div>
            </div>
        </div>
    );
}
