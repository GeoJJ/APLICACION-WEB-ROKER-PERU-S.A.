import { api } from '@/lib/api';
import { useState } from 'react';
import { FileSpreadsheet, Download } from 'lucide-react';

export default function Reportes() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const handleDescargarExcel = async () => {
        setLoading(true);
        setError('');
        try {
            const blob = await api('/reportes/productos/excel', { blob: true });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'productos-stock.xlsx';
            link.click();
            setTimeout(() => URL.revokeObjectURL(url), 1000);
        } catch { setError('No se pudo descargar el reporte. Comprueba que el backend esté disponible.'); }
        finally { setLoading(false); }
    };

    return (
        <div className="max-w-7xl mx-auto">
            {/* Cabecera de la sección */}
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                    <FileSpreadsheet className="text-blue-600" size={32} />
                    Gestión de Reportes Gerenciales
                </h1>
                <p className="text-sm text-gray-500 mt-1">
                    Descarga el inventario y la disponibilidad de productos en formato Excel.
                </p>
            </div>

            {/* Grid de Reportes Disponibles */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 flex flex-col justify-between hover:shadow-md transition-shadow">
                    <div>
                        <div className="flex items-center justify-between mb-4">
                            <span className="p-3 bg-blue-50 text-blue-600 rounded-lg">
                                <FileSpreadsheet size={24} />
                            </span>
                            <span className="text-xs font-semibold px-2.5 py-1 bg-green-50 text-green-700 rounded-full">
                                Activo
                            </span>
                        </div>
                        <h3 className="text-lg font-semibold text-gray-800 mb-2">
                            Reporte de Productos y Stock
                        </h3>
                        <p className="text-sm text-gray-600 mb-6">
                            Genera y descarga un archivo <code className="bg-gray-100 px-1.5 py-0.5 rounded text-xs font-mono text-blue-600">.xlsx</code> con el listado detallado de inventario, categorías y disponibilidad actual.
                        </p>
                    </div>

                    <button 
                        onClick={handleDescargarExcel}
                        disabled={loading}
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 shadow-sm"
                    >
                        <Download size={18} />
                        {loading ? 'Generando...' : 'Descargar Excel'}
                    </button>
                </div>
            </div>
            {error && <p role="alert" className="mt-5 rounded-lg bg-red-50 p-4 text-sm text-red-700">{error}</p>}
        </div>
    );
}
