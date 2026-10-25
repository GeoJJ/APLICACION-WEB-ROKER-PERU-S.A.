import { ShoppingCart } from "lucide-react";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";

export default function ProductCard({ product }) {
    let stockBadge = null;
    if (product.stock === 0) {
        stockBadge = <Badge variant="danger" className="text-xs px-2 py-0.5">Agotado</Badge>;
    } else if (product.stock < 10) {
        stockBadge = <Badge variant="warning" className="text-xs px-2 py-0.5">Stock bajo</Badge>;
    }

    return (
        <div className="group flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            {/* Imagen - más pequeña */}
            <div className="relative aspect-square overflow-hidden bg-gray-100 p-3">
                <img
                    src={product.image}
                    alt={product.name}
                    className="h-full w-full object-contain transition duration-300 group-hover:scale-105"
                />
                {stockBadge && (
                    <div className="absolute left-2 top-2">{stockBadge}</div>
                )}
            </div>

            {/* Información - más compacta */}
            <div className="flex flex-col gap-2 p-4">
                <div>
                    <h3 className="text-sm font-semibold text-gray-900 line-clamp-2">
                        Nombre del Producto
                    </h3>
                    <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">
                        descripcion del producto
                    </p>
                </div>

                <div className="mt-auto pt-2">
                    <p className="text-lg font-bold text-brand">S/.{product.price.toFixed(2)}</p>
                    <Button
                        size="sm"
                        className="mt-2 w-full text-sm"
                        disabled={product.stock === 0}
                    >
                        <ShoppingCart size={16} className="mr-1.5" />
                        Agregar
                    </Button>
                </div>
            </div>
        </div>
    );
}