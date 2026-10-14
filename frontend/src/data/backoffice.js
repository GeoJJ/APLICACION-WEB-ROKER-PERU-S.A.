// src/data/backoffice.js

export const ADMIN_STATS = [
    { label: "VENTAS", value: "S/.45,040", change: "+12%" },
    { label: "SERVICIOS", value: "S/.25,040", change: "+12%" },
    { label: "PEDIDOS", value: "274", change: "+12%" },
    { label: "CLIENTES", value: "12,040", change: "+12%" },
];

export const STOCK_DATA = [
    { id: 1, producto: "Mascarilla N95 Profesional", categoria: "Mascarillas", stock: 120, umbral: 50, estado: "En Stock" },
    { id: 2, producto: "Guantes de Nitrilo (Caja x100)", categoria: "Guantes", stock: 45, umbral: 50, estado: "Stock Bajo" },
    { id: 3, producto: "Desinfectante de Superficies 5L", categoria: "Desinfectantes", stock: 8, umbral: 10, estado: "Crítico" },
    { id: 4, producto: "Traje de Protección Tyvek", categoria: "Trajes", stock: 200, umbral: 50, estado: "En Stock" },
];

export const SELLER_ORDERS = [
    { id: "#PED-00452", cliente: "María López", tipo: "Producto", total: "S/.150.00", estado: "En camino" },
    { id: "#PED-00453", cliente: "Juan Pérez", tipo: "Servicio", total: "S/.200.00", estado: "Pendiente" },
    { id: "#PED-00454", cliente: "Ana García", tipo: "Producto", total: "S/.85.00", estado: "Completado" },
];

export const DELIVERIES = [
    { id: "#PED-00452", cliente: "María López", direccion: "Av. Los Próceres 245, Lima", estado: "En camino", telefono: "987 654 321", tab: "hoy" },
    { id: "#PED-00453", cliente: "Juan Pérez", direccion: "Calle Las Flores 123, Lima", estado: "Pendiente", telefono: "912 345 678", tab: "pendientes" },
    { id: "#PED-00454", cliente: "Ana García", direccion: "Jr. Los Pinos 456, Lima", estado: "Entregado", telefono: "998 765 432", tab: "completados" },
    { id: "#PED-00455", cliente: "Carlos Ruiz", direccion: "Av. Javier Prado 1200, Lima", estado: "En camino", telefono: "955 123 456", tab: "hoy" },
];

export const STOCK_ALERTS = [
    { id: 1, mensaje: "¡Alerta de stock bajo en Guantes de Nitrilo!", tiempo: "Hace 5 minutos" },
    { id: 2, mensaje: "¡Alerta de stock crítico en Desinfectante 5L!", tiempo: "Hace 10 minutos" },
];