import { useState } from 'react';
import { Download, Eye, FileBarChart2, FileSpreadsheet, FileText } from 'lucide-react';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import PdfPreviewModal from '@/components/shared/PdfPreviewModal';
import { fetchInventoryExcel, fetchSalesReportPdf, saveBlob } from '@/lib/reporteApi';

const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio',
    'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
const OPCIONES_MES = MESES.map((label, i) => ({ value: String(i + 1), label }));

// Año actual y los 4 anteriores
const ANIO_ACTUAL = new Date().getFullYear();
const OPCIONES_ANIO = Array.from({ length: 5 }, (_, i) => {
    const y = String(ANIO_ACTUAL - i);
    return { value: y, label: y };
});

const OPCIONES_ALCANCE = [
    { value: 'TODOS', label: 'Productos y servicios' },
    { value: 'PRODUCTOS', label: 'Solo productos' },
    { value: 'SERVICIOS', label: 'Solo servicios' },
];

export default function Reportes() {
    const [error, setError] = useState('');
    const [loadingExcel, setLoadingExcel] = useState(false);
    const [loadingPdf, setLoadingPdf] = useState(false);

    // Filtros del registro de ventas
    const [modo, setModo] = useState('mes'); // 'mes' | 'anio'
    const [anio, setAnio] = useState(String(ANIO_ACTUAL));
    const [mes, setMes] = useState(String(new Date().getMonth() + 1));
    const [alcance, setAlcance] = useState('TODOS');

    const [preview, setPreview] = useState(null); // { title, loader }

    const filtros = () => ({ anio: Number(anio), mes: modo === 'mes' ? Number(mes) : null, alcance });
    const periodoTexto = modo === 'mes' ? `${MESES[Number(mes) - 1]} ${anio}` : `Año ${anio}`;

    const descargarExcel = async () => {
        setLoadingExcel(true);
        setError('');
        try {
            saveBlob(await fetchInventoryExcel());
        } catch (e) {
            setError(e.message);
        } finally {
            setLoadingExcel(false);
        }
    };

    const descargarPdf = async () => {
        setLoadingPdf(true);
        setError('');
        try {
            saveBlob(await fetchSalesReportPdf(filtros()));
        } catch (e) {
            setError(e.message);
        } finally {
            setLoadingPdf(false);
        }
    };

    const abrirVistaPrevia = () => {
        setError('');
        const f = filtros();
        setPreview({ title: `Registro de ventas - ${periodoTexto}`, loader: () => fetchSalesReportPdf(f) });
    };

    return (
        <div className="mx-auto max-w-7xl">
            <div className="mb-8">
                <h1 className="flex items-center gap-3 text-2xl font-bold text-gray-900">
                    <FileBarChart2 className="text-brand" size={32} />
                    Reportes gerenciales
                </h1>
                <p className="mt-1 text-sm text-gray-500">
                    Consulta el registro de ventas por período y descarga el inventario completo.
                </p>
            </div>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                {/* REGISTRO DE VENTAS (PDF) */}
                <div className="flex flex-col justify-between rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
                    <div>
                        <div className="mb-4 flex items-center justify-between">
                            <span className="rounded-lg bg-red-50 p-3 text-red-600"><FileText size={24} /></span>
                            <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-600">PDF</span>
                        </div>
                        <h2 className="mb-1 text-lg font-semibold text-gray-800">Registro de ventas</h2>
                        <p className="mb-5 text-sm text-gray-600">
                            Boletas y facturas emitidas en el período elegido, con subtotal, IGV y total.
                        </p>

                        <div className="mb-4 inline-flex rounded-xl border border-gray-200 p-1 text-sm font-medium">
                            {[['mes', 'Por mes'], ['anio', 'Por año']].map(([value, label]) => (
                                <button
                                    key={value}
                                    type="button"
                                    onClick={() => setModo(value)}
                                    className={`rounded-lg px-4 py-1.5 transition ${modo === value ? 'bg-brand text-white' : 'text-gray-600 hover:bg-gray-100'}`}
                                >
                                    {label}
                                </button>
                            ))}
                        </div>

                        <div className="grid gap-3 sm:grid-cols-2">
                            {modo === 'mes' && (
                                <label className="text-sm">
                                    <span className="mb-1 block font-medium text-gray-700">Mes</span>
                                    <Select aria-label="Mes" options={OPCIONES_MES} value={mes} onChange={e => setMes(e.target.value)} />
                                </label>
                            )}
                            <label className="text-sm">
                                <span className="mb-1 block font-medium text-gray-700">Año</span>
                                <Select aria-label="Año" options={OPCIONES_ANIO} value={anio} onChange={e => setAnio(e.target.value)} />
                            </label>
                            <label className="text-sm sm:col-span-2">
                                <span className="mb-1 block font-medium text-gray-700">Incluir</span>
                                <Select aria-label="Incluir" options={OPCIONES_ALCANCE} value={alcance} onChange={e => setAlcance(e.target.value)} />
                            </label>
                        </div>
                    </div>

                    <div className="mt-6 flex flex-wrap gap-3">
                        <Button onClick={abrirVistaPrevia} className="flex-1">
                            <Eye size={18} className="mr-2" />
                            Vista previa
                        </Button>
                        <Button variant="outline" onClick={descargarPdf} disabled={loadingPdf} className="flex-1">
                            <Download size={18} className="mr-2" />
                            {loadingPdf ? 'Generando...' : 'Descargar PDF'}
                        </Button>
                    </div>
                </div>

                {/* INVENTARIO (EXCEL) */}
                <div className="flex flex-col justify-between rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
                    <div>
                        <div className="mb-4 flex items-center justify-between">
                            <span className="rounded-lg bg-green-50 p-3 text-green-600"><FileSpreadsheet size={24} /></span>
                            <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-600">Excel</span>
                        </div>
                        <h2 className="mb-1 text-lg font-semibold text-gray-800">Registro de inventario</h2>
                        <p className="text-sm text-gray-600">
                            Todos los productos con código, categoría, proveedor, precio, stock, stock mínimo y valor en stock.
                            Marca en colores los productos con stock bajo o agotados, e incluye filtros y totales.
                        </p>
                    </div>

                    <Button onClick={descargarExcel} disabled={loadingExcel} className="mt-6 w-full">
                        <Download size={18} className="mr-2" />
                        {loadingExcel ? 'Generando...' : 'Descargar Excel'}
                    </Button>
                </div>
            </div>

            {error && <p role="alert" className="mt-5 rounded-lg bg-red-50 p-4 text-sm text-red-700">{error}</p>}

            {preview && <PdfPreviewModal title={preview.title} loader={preview.loader} onClose={() => setPreview(null)} />}
        </div>
    );
}