export const money = value => new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(Number(value) || 0);
export const stockState = p => p.stock <= p.umbral / 2 ? 'Crítico' : p.stock < p.umbral ? 'Stock Bajo' : 'En Stock';
export const localDate = () => new Date().toLocaleDateString('en-CA');
