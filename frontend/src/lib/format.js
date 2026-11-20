export function money(value) {
    const amount = Number(value ?? 0);
    return new Intl.NumberFormat('es-PE', {
        style: 'currency',
        currency: 'PEN',
        minimumFractionDigits: 2,
    }).format(Number.isNaN(amount) ? 0 : amount);
}