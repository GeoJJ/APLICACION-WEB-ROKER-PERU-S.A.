 import http from './axiosConfig';

// Cuando se pide un blob, los errores del servidor (ProblemDetail JSON) también llegan como Blob.
async function errorMessage(e) {
    if (!e.response) return 'No se pudo conectar con el backend. Comprueba que esté ejecutándose.';
    const { status, data } = e.response;
    if (data instanceof Blob) {
        try {
            const json = JSON.parse(await data.text());
            if (json?.detail || json?.message) return json.detail || json.message;
        } catch {
            /* el cuerpo no era JSON */
        }
    }
    if (status === 403) return 'No tienes permiso para generar este reporte.';
    return `No se pudo generar el reporte (${status}).`;
}

function filenameOf(res, fallback) {
    const match = /filename="?([^";]+)"?/i.exec(res.headers?.['content-disposition'] || '');
    return match?.[1] || fallback;
}

/**
 * Registro de ventas en PDF.
 * @param {{ anio: number, mes?: number|null, alcance?: 'TODOS'|'PRODUCTOS'|'SERVICIOS' }} filtros
 *        mes = null/undefined -> todo el año
 * @returns {Promise<{ blob: Blob, filename: string }>}
 */
export async function fetchSalesReportPdf({ anio, mes = null, alcance = 'TODOS' }) {
    try {
        const params = { anio, alcance };
        if (mes) params.mes = mes;
        const res = await http.get('/gerente/reportes/ventas/pdf', { params, responseType: 'blob' });
        return {
            blob: new Blob([res.data], { type: 'application/pdf' }),
            filename: filenameOf(res, `registro-ventas-${anio}.pdf`),
        };
    } catch (e) {
        throw new Error(await errorMessage(e), { cause: e });
    }
}

/** Registro de inventario completo en Excel. */
export async function fetchInventoryExcel() {
    try {
        const res = await http.get('/gerente/reportes/productos/excel', { responseType: 'blob' });
        return {
            blob: new Blob([res.data], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
            }),
            filename: filenameOf(res, 'inventario.xlsx'),
        };
    } catch (e) {
        throw new Error(await errorMessage(e), { cause: e });
    }
}

/** Dispara la descarga de un blob en el navegador. */
export function saveBlob({ blob, filename }) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
}