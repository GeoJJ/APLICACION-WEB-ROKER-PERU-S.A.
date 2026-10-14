import { Camera } from "lucide-react";
import Button from "@/components/ui/Button";
import Select from "@/components/ui/Select";
import Textarea from "@/components/ui/Textarea";

export default function DeliveryDetail({ delivery }) {
    if (!delivery) {
        return (
            <div className="flex h-64 items-center justify-center rounded-2xl border-2 border-dashed border-gray-200 bg-gray-50 text-gray-500">
                Selecciona un pedido de la lista para ver los detalles
            </div>
        );
    }

    return (
        <div className="grid gap-6 lg:grid-cols-2">
            {/* Columna Izquierda: Información y Formulario */}
            <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm space-y-5">
                <h3 className="text-lg font-semibold text-gray-900">Detalle de Entrega</h3>

                <div className="grid grid-cols-3 gap-4 text-sm">
                    <span className="font-medium text-gray-600">Pedido #:</span>
                    <span className="col-span-2 font-mono font-semibold text-gray-900">{delivery.id}</span>

                    <span className="font-medium text-gray-600">Solicitante:</span>
                    <span className="col-span-2 text-gray-900">{delivery.cliente}</span>

                    <span className="font-medium text-gray-600">Teléfono:</span>
                    <span className="col-span-2 text-gray-900">{delivery.telefono}</span>

                    <span className="font-medium text-gray-600">Dirección:</span>
                    <span className="col-span-2 text-gray-900">{delivery.direccion}</span>
                </div>

                <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Estado de entrega</label>
                    <Select
                        options={[
                            { value: "Pendiente", label: "Pendiente" },
                            { value: "En camino", label: "En camino" },
                            { value: "Entregado", label: "Entregado" }
                        ]}
                        defaultValue={delivery.estado}
                    />
                </div>

                <div className="space-y-2">
                    <label className="text-sm font-medium text-gray-700">Observaciones</label>
                    <Textarea placeholder="Ingrese observaciones de la entrega..." rows={3} />
                </div>

                <Button className="w-full">Marcar como Entregado</Button>
            </div>

            {/* Columna Derecha: Evidencia */}
            <div className="space-y-6">
                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h3 className="mb-4 text-lg font-semibold text-gray-900">Evidencia de Entrega</h3>
                    <div className="relative overflow-hidden rounded-xl bg-gray-100">
                        <img
                            src="https://placehold.co/600x400/e2e8f0/1e3a8a?text=Foto+de+Entrega"
                            alt="Evidencia"
                            className="h-64 w-full object-cover"
                        />
                        <button className="absolute bottom-4 left-1/2 flex -translate-x-1/2 items-center gap-2 rounded-xl bg-brand px-6 py-2.5 font-semibold text-white transition hover:bg-brand/90">
                            <Camera size={18} /> Tomar Foto
                        </button>
                    </div>
                </div>

                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h3 className="mb-4 text-lg font-semibold text-gray-900">Firma del Cliente</h3>
                    <div className="flex h-40 items-center justify-center rounded-xl border-2 border-dashed border-gray-300 bg-gray-50">
                        <img
                            src="https://placehold.co/400x200/ffffff/1e3a8a?text=Firma+Digital"
                            alt="Firma"
                            className="h-32 object-contain opacity-70"
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}