import { useCart } from '@/components/cart/CartProvider';
import { ShoppingCart } from 'lucide-react';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { FALLBACK_IMAGE } from '@/lib/api';

export default function ProductCard({ product }) {
    const { add } = useCart();

    let stockBadge = null;
    if (product.stock === 0) {
        stockBadge = (
            <Badge variant="danger" className="px-2 py-0.5 text-xs">
                Agotado
            </Badge>
        );
    } else if (product.stock != null && product.stock < 10) {
        stockBadge = (
            <Badge variant="warning" className="px-2 py-0.5 text-xs">
                Stock bajo
            </Badge>
        );
    }

    return (
        <div className="group flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            {/* Imagen */}
            <div className="relative aspect-square overflow-hidden border-b border-gray-100 bg-white p-4">
                <img
                    src={product.image || FALLBACK_IMAGE}
                    alt={product.name}
                    loading="lazy"
                    onError={(e) => {
                        if (e.currentTarget.dataset.fallback) return;
                        e.currentTarget.dataset.fallback = 'true';
                        e.currentTarget.src = FALLBACK_IMAGE;
                    }}
                    className="h-full w-full object-contain transition duration-300 group-hover:scale-105"
                />
                {stockBadge && (
                    <div className="absolute left-2 top-2">{stockBadge}</div>
                )}
            </div>

            {/* Información */}
            <div className="flex flex-1 flex-col gap-2 p-4">
                <div>
                    <p className="text-[11px] font-medium uppercase tracking-wide text-gray-400 line-clamp-1">
                        {product.category}
                    </p>
                    <h3 className="mt-0.5 text-sm font-semibold text-gray-900 line-clamp-2">
                        {product.name}
                    </h3>
                </div>

                <div className="mt-auto pt-2">
                    <p className="text-lg font-bold text-brand">
                        S/. {Number(product.price ?? 0).toFixed(2)}
                    </p>
                    <Button
                        size="sm"
                        className="mt-2 w-full text-sm"
                        disabled={product.stock === 0}
                        onClick={() => add(product)}
                    >
                        <ShoppingCart size={16} className="mr-1.5" />
                        Agregar
                    </Button>
                </div>
            </div>
        </div>
    );
}