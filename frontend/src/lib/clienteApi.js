import { api } from '@/lib/api';

export function fetchClientProfile() {
    return api('/cliente/perfil');
}

export function updateClientProfile(payload) {
    return api('/cliente/perfil', {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
}

export function fetchClientOrders() {
    return api('/cliente/pedidos');
}

export function createClientOrder(payload) {
    return api('/cliente/pedidos', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}

export function fetchClientServiceOrders() {
    return api('/cliente/ordenes-servicio');
}

export function fetchClientServiceOrderById(idOrden) {
    return api(`/cliente/ordenes-servicio/${idOrden}`);
}

export function createClientServiceOrder(payload) {
    return api('/cliente/ordenes-servicio', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
}