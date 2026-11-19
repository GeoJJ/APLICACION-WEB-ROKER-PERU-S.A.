import { request } from './api';

const text = value => String(value ?? '').trim();
const optional = value => text(value) || null;

// Un recurso por cada controller /admin/*. Para cada uno:
//   path → ruta del controller
//   body → convierte el registro de la interfaz (nombres cortos: nombre, email, precio…)
//          en el JSON exacto que espera su Request DTO del backend.
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
            codigoProducto: text(r.codigo),
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

// type: 'ingreso' (suma stock) | 'ajuste' (RESTA stock: el backend lo trata como una salida)
export function postMovement(type, { productId, cantidad }) {
    return request(`/admin/inventario/${type}`, {
        method: 'POST',
        body: JSON.stringify({ productoId: Number(productId), cantidad: Number(cantidad) }),
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
