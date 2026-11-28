import { request } from './api';

const text = value => String(value ?? '').trim();
const optional = value => text(value) || null;

export const ADMIN_RESOURCES = {
    workers: {
        path: '/admin/usuarios',
        body: r => ({
            nombreUsuario: text(r.nombre),
            correoUsuario: text(r.email),
            // El backend exige que coincida con el documento: 8 dígitos (DNI) o 11 (RUC)
            tipoPersonaUsuario: text(r.documento).length === 11 ? 'JURIDICA' : 'NATURAL',
            numeroDocumentoUsuario: text(r.documento),
            telefonoUsuario: optional(r.telefono),
            direccionUsuario: optional(r.direccion),
            idRol: Number(r.idRol),
            // Al editar, sin contraseña = no se cambia (el campo se omite del JSON)
            passwordUsuario: optional(r.password) ?? undefined,
        }),
    },
    products: {
        path: '/admin/productos',
        body: r => ({
            // El código (PRD-0001) lo genera el backend: no se envía
            nombreProducto: text(r.producto),
            categoriaProducto: optional(r.categoria),
            precioProducto: Number(r.precio),
            // El backend solo usa el stock al CREAR; después cambia con movimientos de inventario
            stockProducto: Number(r.stock) || 0,
            stockMinimo: Number(r.umbral) || 0,
            estadoProducto: r.estado === 'Inactivo' ? 'INACTIVO' : 'ACTIVO',
            idProveedor: Number(r.idProveedor),
        }),
    },
    services: {
        path: '/admin/servicios',
        body: r => ({
            categoriaServicio: optional(r.categoria),
            nombreServicio: text(r.nombre),
            tipoServicio: optional(r.tipo),
            descripcionServicio: optional(r.descripcion),
            precioServicio: Number(r.precio),
            duracionEstimadaServicio: optional(r.duracion),
            cantidadPersonalServicio: optional(r.personal),
        }),
    },
    providers: {
        path: '/admin/proveedores',
        body: r => ({
            nombreProveedor: text(r.nombre),
            rucProveedor: text(r.ruc),
            telefonoProveedor: optional(r.telefono),
            correoProveedor: optional(r.email),
        }),
    },
    employees: {
        path: '/admin/empleados-servicio',
        body: r => ({
            nombreEmpleadoServicio: text(r.nombre),
            apellidoEmpleadoServicio: text(r.apellido),
            telefonoEmpleadoServicio: optional(r.telefono),
        }),
    },
    roles: {
        path: '/admin/roles',
        body: r => ({ nombreRol: text(r.nombre) }),
    },
};

// Crear (POST) si el registro no tiene id; actualizar (PUT) si lo tiene.
export function saveResource(kind, record) {
    const { path, body } = ADMIN_RESOURCES[kind];
    const creating = record.id == null;
    return request(creating ? path : `${path}/${record.id}`, {
        method: creating ? 'POST' : 'PUT',
        body: JSON.stringify(body(record)),
    });
}

export function deleteResource(kind, id) {
    return request(`${ADMIN_RESOURCES[kind].path}/${id}`, { method: 'DELETE' });
}

// Vuelve a activar un registro desactivado (por ahora solo trabajadores)
export function reactivateResource(kind, id) {
    return request(`${ADMIN_RESOURCES[kind].path}/${id}/reactivar`, { method: 'PUT' });
}

// type: 'ingreso' (suma stock) | 'ajuste' (RESTA stock: el backend lo trata como una salida)
export function postMovement(type, { productId, cantidad }, role = 'admin') {
    const base = role === 'warehouse' ? '/almacenista' : '/admin';
    return request(`${base}/inventario/${type}`, {
        method: 'POST',
        body: JSON.stringify({ productoId: Number(productId), cantidad: Number(cantidad) }),
    });
}
export function putDeliveryState(idPedido, { estado, observaciones }) {
    return request(`/repartidor/pedidos/${idPedido}/estado`, {
        method: 'PUT',
        body: JSON.stringify({
            nuevoEstado: estado === 'Entregado' ? 'ENTREGADO' : 'EN_RUTA',
            incidentePedido: observaciones?.trim() || null,
        }),
    });
}
// Consultar historial completo de movimientos
export function fetchMovements() {
    return request('/admin/inventario/movimientos');
}

// Consultar alertas de stock
export function fetchStockAlerts() {
    return request('/admin/inventario/alertas');
}

// Consultar stock actualizado de un producto específico
export function fetchProductStock(productId) {
    return request(`/admin/inventario/stock/${productId}`);
}