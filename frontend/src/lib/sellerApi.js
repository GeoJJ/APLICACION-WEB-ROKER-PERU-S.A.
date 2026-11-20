import { request } from './api';

const text = value => String(value ?? '').trim();

function toLocalDateTime(value) {
    const clean = text(value);
    if (!clean) return null;
    return clean.length === 16 ? `${clean}:00` : clean;
}

// ---------- Pedidos de productos ----------
export function fetchSellerOrders() {
    return request('/vendedor/pedidos');
}

export function confirmSellerOrder(orderId, { idDelivery, fechaSalidaPedido, fechaEntregaPedido }) {
    return request(`/vendedor/pedidos/${orderId}/confirmar`, {
        method: 'PUT',
        body: JSON.stringify({
            idDelivery: Number(idDelivery),
            fechaSalidaPedido: toLocalDateTime(fechaSalidaPedido),
            fechaEntregaPedido: toLocalDateTime(fechaEntregaPedido),
        }),
    });
}

export function fetchDeliveryDrivers() {
    return request('/vendedor/repartidores');
}

// ---------- Ventas ----------
export function fetchSellerSales() {
    return request('/vendedor/pedidos/mis-ventas');
}

// ---------- Solicitudes / órdenes de servicio ----------
export function fetchServiceOrders() {
    return request('/vendedor/ordenes-servicio');
}

export function fetchServiceOrderById(idOrden) {
    return request(`/vendedor/ordenes-servicio/${idOrden}`);
}

export function confirmServiceOrder(orderId, incidencia = '') {
    const clean = text(incidencia);
    const query = clean ? `?${new URLSearchParams({ incidencia: clean }).toString()}` : '';
    return request(`/vendedor/ordenes-servicio/${orderId}/confirmar${query}`, { method: 'PUT' });
}

export function cancelServiceOrder(orderId, incidencia = '') {
    const clean = text(incidencia);
    const query = clean ? `?${new URLSearchParams({ incidencia: clean }).toString()}` : '';
    return request(`/vendedor/ordenes-servicio/${orderId}/cancelar${query}`, { method: 'PUT' });
}

export function fetchServiceEmployees() {
    return request('/vendedor/empleados-servicio');
}

export function searchServiceEmployees(texto) {
    const query = new URLSearchParams({ texto: text(texto) }).toString();
    return request(`/vendedor/empleados-servicio/buscar?${query}`);
}

export function fetchAssignments() {
    return request('/vendedor/asignaciones');
}

export function fetchAssignmentsByOrder(orderId) {
    return request(`/vendedor/asignaciones/orden/${orderId}`);
}

export function fetchAssignmentsByEmployee(idEmpleado) {
    return request(`/vendedor/asignaciones/empleado/${idEmpleado}`);
}

export function fetchAssignmentById(id) {
    return request(`/vendedor/asignaciones/${id}`);
}

export function createAssignment({ idOrdenServicio, idEmpleado, nombreResponsableAsignacion, descripcionAsignacion }) {
    return request('/vendedor/asignaciones', {
        method: 'POST',
        body: JSON.stringify({
            idOrdenServicio: Number(idOrdenServicio),
            idEmpleado: Number(idEmpleado),
            nombreResponsableAsignacion: text(nombreResponsableAsignacion),
            descripcionAsignacion: text(descripcionAsignacion),
        }),
    });
}

// ---------- Catálogo de productos ----------
export function fetchSellerProducts() {
    return request('/vendedor/productos');
}

export function searchSellerProducts(texto) {
    const query = new URLSearchParams({ texto: text(texto) }).toString();
    return request(`/vendedor/productos/buscar?${query}`);
}

export function fetchSellerProductById(id) {
    return request(`/vendedor/productos/${id}`);
}

// ---------- Catálogo de servicios ----------
export function fetchSellerServices() {
    return request('/vendedor/servicios');
}

export function fetchSellerServiceById(id) {
    return request(`/vendedor/servicios/${id}`);
}

// ---------- Inventario (salidas y stock) ----------
export function registerStockExit(payload) {
    return request('/vendedor/inventario/salida', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}

export function fetchSellerProductStock(idProducto) {
    return request(`/vendedor/inventario/stock/${idProducto}`);
}

export function fetchAllSellerSales(idVendedor = null) {
    const query = idVendedor ? `?${new URLSearchParams({ idVendedor: String(idVendedor) }).toString()}` : '';
    return request(`/vendedor/pedidos/ventas${query}`);
}

export function updateSellerOrder(orderId, { idDelivery, fechaSalidaPedido, fechaEntregaPedido }) {
    return request(`/vendedor/pedidos/${orderId}`, {
        method: 'PUT',
        body: JSON.stringify({
            idDelivery: Number(idDelivery),
            fechaSalidaPedido: toLocalDateTime(fechaSalidaPedido),
            fechaEntregaPedido: toLocalDateTime(fechaEntregaPedido),
        }),
    });
}

export function createInStoreOrder({ metodoPagoPedido, direccionCliente = 'Venta presencial en tienda', detalles, idCliente = null }) {
    const query = idCliente ? `?${new URLSearchParams({ idCliente: String(idCliente) }).toString()}` : '';
    return request(`/vendedor/pedidos/presencial${query}`, {
        method: 'POST',
        body: JSON.stringify({
            metodoPagoPedido,
            direccionCliente,
            detalles,
        }),
    });
}