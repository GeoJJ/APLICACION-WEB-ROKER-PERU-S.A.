import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, Users, Tag, X } from 'lucide-react';
import Button from '@/components/ui/Button';
import { money } from '@/lib/format';
import { FALLBACK_IMAGE } from '@/lib/api';

function formatPersonnel(value) {
    if (!value) return 'Por definir';
    const num = Number(value);
    if (!Number.isNaN(num)) return `${num} ${num === 1 ? 'técnico' : 'técnicos'}`;
    return value;
}

function handleImageError(e) {
    if (e.currentTarget.dataset.fallback) return;
    e.currentTarget.dataset.fallback = 'true';
    e.currentTarget.src = FALLBACK_IMAGE;
}

export default function ServiceCard({ service }) {
    const [open, setOpen] = useState(false);

    useEffect(() => {
        if (!open) return;
        const onKeyDown = (e) => {
            if (e.key === 'Escape') setOpen(false);
        };
        window.addEventListener('keydown', onKeyDown);
        return () => window.removeEventListener('keydown', onKeyDown);
    }, [open]);

    const requestUrl = `/servicios/${encodeURIComponent(service.id)}/solicitar`;
    const imageSrc = service.image || FALLBACK_IMAGE;

    return (
        <>
            <div
                onClick={() => setOpen(true)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        setOpen(true);
                    }
                }}
                className="group flex cursor-pointer flex-col overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
            >
                {/* Imagen */}
                <div className="relative aspect-square overflow-hidden bg-gray-100">
                    <img
                        src={imageSrc}
                        alt={service.name}
                        loading="lazy"
                        onError={handleImageError}
                        className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                    />
                </div>

                {/* Información */}
                <div className="flex flex-1 flex-col gap-2 p-4">
                    <div>
                        <h3 className="text-sm font-semibold text-gray-900 line-clamp-2">
                            {service.name}
                        </h3>
                        <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">
                            {service.description}
                        </p>
                    </div>

                    <div className="mt-auto pt-2">
                        <p className="text-sm font-semibold text-brand">
                            {service.price != null ? money(service.price) : 'Consultar tarifa'}
                        </p>
                        <Button
                            as={Link}
                            to={requestUrl}
                            onClick={(e) => e.stopPropagation()}
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

            {/* Modal de detalle */}
            {open && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-[1px]"
                    onClick={() => setOpen(false)}
                    role="dialog"
                    aria-modal="true"
                    aria-labelledby={`service-modal-${service.id}`}
                >
                    <div
                        className="relative w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-xl"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <button
                            type="button"
                            onClick={() => setOpen(false)}
                            aria-label="Cerrar detalle"
                            className="absolute right-3 top-3 z-10 rounded-full bg-white/90 p-1.5 text-gray-600 shadow hover:bg-gray-100 hover:text-gray-900"
                        >
                            <X size={18} />
                        </button>

                        <div className="h-52 w-full overflow-hidden bg-gray-100">
                            <img
                                src={imageSrc}
                                alt={service.name}
                                onError={handleImageError}
                                className="h-full w-full object-cover"
                            />
                        </div>

                        <div className="space-y-4 p-6">
                            <div>
                                {service.category && (
                                    <span className="inline-flex items-center gap-1 rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium capitalize text-gray-700">
                                        <Tag size={12} />
                                        {service.category}
                                    </span>
                                )}
                                <h2
                                    id={`service-modal-${service.id}`}
                                    className="mt-2 text-lg font-bold text-gray-900"
                                >
                                    {service.name}
                                </h2>
                                <p className="mt-1.5 text-sm leading-relaxed text-gray-600">
                                    {service.description || 'Sin descripción adicional.'}
                                </p>
                            </div>

                            <div className="grid grid-cols-3 gap-2 rounded-xl bg-gray-50 p-3 text-center">
                                <div>
                                    <p className="text-[11px] font-medium uppercase text-gray-500">
                                        Tarifa
                                    </p>
                                    <p className="mt-0.5 text-sm font-semibold text-brand">
                                        {service.price != null ? money(service.price) : 'Consultar'}
                                    </p>
                                </div>
                                <div>
                                    <p className="flex items-center justify-center gap-1 text-[11px] font-medium uppercase text-gray-500">
                                        <Clock size={12} /> Duración
                                    </p>
                                    <p className="mt-0.5 text-sm font-semibold text-gray-900">
                                        {service.duration || 'Por definir'}
                                    </p>
                                </div>
                                <div>
                                    <p className="flex items-center justify-center gap-1 text-[11px] font-medium uppercase text-gray-500">
                                        <Users size={12} /> Personal
                                    </p>
                                    <p className="mt-0.5 text-sm font-semibold text-gray-900">
                                        {formatPersonnel(service.personnel)}
                                    </p>
                                </div>
                            </div>

                            <div className="flex items-center justify-end gap-2 pt-2">
                                <Button
                                    type="button"
                                    variant="secondary"
                                    size="sm"
                                    onClick={() => setOpen(false)}
                                >
                                    Cerrar
                                </Button>
                                <Button as={Link} to={requestUrl} size="sm">
                                    <Calendar size={16} className="mr-1.5" />
                                    Solicitar servicio
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}