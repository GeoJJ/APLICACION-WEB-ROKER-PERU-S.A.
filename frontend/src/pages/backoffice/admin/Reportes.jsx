import React from 'react';
import { FileSpreadsheet, Download, ShieldAlert } from 'lucide-react';

export default function Reportes() {
    const handleDescargarExcel = () => {
        // Redirige directamente al endpoint del backend para descargar el Excel
        window.location.href = 'http://localhost:8080/api/reportes/productos/excel';
    };

    return (
        <div className="p-8 max-w-7xl mx-auto">
            {/* Cabecera de la sección */}
            <div className="mb-8">
                <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                    <FileSpreadsheet className="text-blue-600" size={32} />
                    Gestión de Reportes Gerenciales
                </h1>
                <p className="text-sm text-gray-500 mt-1">
                    Panel exclusivo para la exportación de estadísticas y analíticas del sistema en formato Excel (CU-03).
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
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 shadow-sm"
                    >
                        <Download size={18} />
                        Descargar Excel
                    </button>
                </div>
            </div>
        </div>
    );
}