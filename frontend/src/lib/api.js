import http from './axiosConfig';

export const pending = feature => new Error(feature + ' pendiente de integración con el backend de esta rama.');

// Todas las llamadas al backend pasan por aquí. La conexión (token, sesión vencida) vive en axiosConfig.js;
// esta función solo convierte los errores de axios en mensajes legibles.
export async function request(path, options = {}) {
    const { blob, body, method = 'GET', headers } = options;
    try {
        const response = await http.request({ url: path, method, data: body, headers, responseType: blob ? 'blob' : 'json' });
        return response.data === '' ? null : response.data;
    } catch (e) {
        if (!e.response) throw new Error('No se pudo conectar con el backend. Comprueba que esté ejecutándose.', { cause: e });
        const { status, data } = e.response;
        // Spring devuelve el motivo en "detail" (formato ProblemDetail)
        const error = new Error(data?.detail || data?.message || data?.mensaje || ('No se pudo consultar el servidor (' + status + ').'), { cause: e });
        error.status = status;
        throw error;
    }
}

export function publicProduct(p) { return { id: p.idProducto, name: p.nombreProducto, category: p.categoriaProducto, price: p.precioProducto, stock: p.stockProducto ?? (p.disponible ? null : 0), available: p.disponible !== false }; }
export function publicService(s) { return { id: s.idServicio ?? s.nombreServicio, name: s.nombreServicio, category: s.categoriaServicio, description: s.descripcionServicio, price: s.precioServicio, duration: s.duracionEstimadaServicio, personnel: s.cantidadPersonalServicio }; }

export async function api(path, options = {}) {
    if (path.startsWith('/checkout')) throw pending('Confirmación de compras');
    if (path.startsWith('/panel')) throw pending('Gestión');
    if (path === '/cliente/productos') return (await request(path, options)).map(publicProduct);
    if (path === '/cliente/servicios') return (await request(path, options)).map(publicService);
    return request(path, options);
}