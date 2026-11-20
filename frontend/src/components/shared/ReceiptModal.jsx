import { useEffect, useRef, useState } from 'react';
import { Download, ExternalLink, FileText, X } from 'lucide-react';
import Button from '@/components/ui/Button';
import { fetchReceiptPdf } from '@/lib/comprobanteApi';

// Botón "Comprobante": abre el visor. Se deshabilita si el pedido/orden aún no tiene comprobante
// (el backend lo emite recién cuando el vendedor confirma).
export function ReceiptButton({ disponible, onClick, size = 'sm', variant = 'outline' }) {
    return (
        <Button
            variant={variant}
            size={size}
            disabled={!disponible}
            onClick={onClick}
            title={disponible ? 'Ver comprobante' : 'Aún no tiene comprobante (se emite al confirmar)'}
        >
            <FileText size={16} className="mr-1.5" />
            Comprobante
        </Button>
    );
}

// Visor del PDF + descarga. kind: 'pedido' | 'servicio'
export default function ReceiptModal({ kind, id, numero, onClose }) {
    const ref = useRef(null);
    const [state, setState] = useState({ loading: true, error: '', url: '', filename: '' });

    useEffect(() => {
        const dialog = ref.current;
        dialog.showModal();
        return () => dialog.close();
    }, []);

    useEffect(() => {
        let objectUrl = '';
        let cancelled = false;
        fetchReceiptPdf(kind, id)
            .then(({ blob, filename }) => {
                if (cancelled) return;
                objectUrl = URL.createObjectURL(blob);
                setState({ loading: false, error: '', url: objectUrl, filename });
            })
            .catch(err => {
                if (!cancelled) setState({ loading: false, error: err.message, url: '', filename: '' });
            });
        return () => {
            cancelled = true;
            if (objectUrl) URL.revokeObjectURL(objectUrl); // libera la memoria del blob
        };
    }, [kind, id]);

    const { loading, error, url, filename } = state;

    return (
        <dialog
            ref={ref}
            onCancel={onClose}
            className="m-auto flex h-[90vh] w-[min(96vw,960px)] flex-col rounded-2xl border border-gray-200 bg-white p-6 shadow-xl backdrop:bg-slate-950/50"
        >
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <h2 className="text-xl font-semibold">Comprobante {numero ? `${numero}` : `#${id}`}</h2>
                <div className="flex items-center gap-2">
                    {url && (
                        <>
                            <Button as="a" href={url} target="_blank" rel="noreferrer" variant="outline" size="sm">
                                <ExternalLink size={16} className="mr-1.5" />
                                Nueva pestaña
                            </Button>
                            <Button as="a" href={url} download={filename} size="sm">
                                <Download size={16} className="mr-1.5" />
                                Descargar PDF
                            </Button>
                        </>
                    )}
                    <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg p-2 hover:bg-gray-100">
                        <X size={22} />
                    </button>
                </div>
            </div>

            <div className="min-h-0 flex-1 overflow-hidden rounded-xl border border-gray-200 bg-gray-50">
                {loading && <p className="p-6 text-sm text-gray-500">Generando comprobante...</p>}
                {error && (
                    <p role="alert" className="m-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </p>
                )}
                {url && <iframe title="Comprobante PDF" src={url} className="h-full w-full" />}
            </div>
        </dialog>
    );
}