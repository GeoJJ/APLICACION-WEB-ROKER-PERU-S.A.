import http from './axiosConfig';

const PATHS = {
    pedido: id => `/comprobantes/pedidos/${id}`,
    servicio: id => `/comprobantes/ordenes-servicio/${id}`,
};

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
    if (status === 403) return 'No tienes permiso para ver este comprobante.';
    if (status === 404) return 'El comprobante no existe.';
    return `No se pudo obtener el comprobante (${status}).`;
}

/**
 * @param {'pedido'|'servicio'} kind
 * @param {number|string} id  idPedido o idOrdenServicio
 * @returns {Promise<{ blob: Blob, filename: string }>}
 */
export async function fetchReceiptPdf(kind, id) {
    try {
        const res = await http.get(PATHS[kind](id), { responseType: 'blob' });
        const disposition = res.headers?.['content-disposition'] || '';
        const match = /filename="?([^";]+)"?/i.exec(disposition);
        return {
            blob: new Blob([res.data], { type: 'application/pdf' }),
            filename: match?.[1] || `comprobante-${id}.pdf`,
        };
    } catch (e) {
        throw new Error(await errorMessage(e), { cause: e });
    }
}