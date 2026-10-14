import { Calendar } from "lucide-react";
import Button from "@/components/ui/Button";

export default function ServiceCard({ service }) {
    return (
        <div className="group flex flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
            {/* Imagen - más pequeña */}
            <div className="relative aspect-square overflow-hidden bg-gray-100 p-3">
                <img
                    src={service.image}
                    alt={service.name}
                    className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                />
            </div>

            {/* Información - más compacta */}
            <div className="flex flex-col gap-2 p-4">
                <div>
                    <h3 className="text-sm font-semibold text-gray-900 line-clamp-2">
                        Nombre del servicio
                    </h3>
                    <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">
                        descripcion del servicio
                    </p>
                </div>

                <div className="mt-auto pt-2">
                    <p className="text-lg font-bold text-brand">S/.{service.price.toFixed(2)}</p>
                    <Button
                        variant="secondary"
                        size="sm"
                        className="mt-2 w-full text-sm"
                    >
                        <Calendar size={16} className="mr-1.5" />
                        Solicitar
                    </Button>
                </div>
            </div>
        </div>
    );
}   