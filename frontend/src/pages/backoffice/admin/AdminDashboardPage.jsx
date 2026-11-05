import { BarChart3 } from 'lucide-react';
import { SectionHeader, Panel, Summary } from '@/components/shared/ManagementUI';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { money } from '@/data/backofficeStore';

export default function AdminDashboardPage() {
    const { data } = useBackOffice();
    const sales = data.orders.filter(o => o.estado === 'Completado');
    const total = sales.reduce((sum, o) => sum + o.total, 0);
    const categories = sales.reduce((result, o) => {
        const category = o.tipo === 'Servicio' ? 'Servicios' : data.products.find(p => p.producto === o.concepto)?.categoria || 'Productos';
        result[category] = (result[category] || 0) + o.total;
        return result;
    }, {});
    const palette = ['#00428a', '#2563eb', '#60a5fa', '#93c5fd', '#0f766e'];
    let offset = 0;
    const segments = Object.entries(categories).map(([label, value], i) => { const start = offset; offset += value / (total || 1) * 100; return { label, value, color: palette[i % palette.length], gradient: `${palette[i % palette.length]} ${start}% ${offset}%` }; });
    const days = Array.from({ length: 7 }, (_, i) => { const d = new Date(); d.setDate(d.getDate() - 6 + i); const date = d.toLocaleDateString('en-CA'); return { date, label: d.toLocaleDateString('es-PE', { weekday: 'short' }), value: sales.filter(o => o.fecha === date).reduce((s, o) => s + o.total, 0) }; });
    return (
        <div className="space-y-8">
            <SectionHeader icon={BarChart3} title="Dashboard" description="Resumen de ventas, servicios y actividad del sistema." />
            <Summary items={[["Ventas", money(total)], ['Servicios', money(sales.filter(o => o.tipo === 'Servicio').reduce((s, o) => s + o.total, 0))], ['Pedidos', data.orders.length], ['Clientes', new Set(data.orders.map(o => o.cliente.toLowerCase())).size]]} />

            {/* Placeholders para gráficos (como solicitaste) */}
            <div className="grid gap-6 lg:grid-cols-2">
                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-lg font-semibold">Ventas por Categoría</h2>
                    <div className="flex min-h-72 flex-wrap items-center justify-center gap-8">{total > 0 ? <><div role="img" aria-label={`Ventas por categoría: ${segments.map(s => `${s.label} ${money(s.value)}`).join(', ')}`} className="h-48 w-48 rounded-full" style={{ background: `conic-gradient(${segments.map(s => s.gradient).join(',')})` }} /><ul className="space-y-3 text-sm">{segments.map(s => <li key={s.label} className="flex items-center gap-2"><span className="h-3 w-3 rounded-full" style={{ background: s.color }} />{s.label}: {money(s.value)}</li>)}</ul></> : <p className="text-gray-500">Aún no hay ventas completadas.</p>}</div>
                </div>
                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-lg font-semibold">Ventas últimos 7 días</h2>
                    <div className="flex h-72 items-end gap-3 pt-8">{days.map(d => <div key={d.date} className="flex h-full min-w-0 flex-1 flex-col justify-end text-center"><span className="mb-2 text-[10px] text-gray-500">{money(d.value)}</span><div title={`${d.date}: ${money(d.value)}`} className="mx-auto w-full max-w-12 rounded-t-lg bg-blue-600" style={{ height: `${Math.max(1, d.value / Math.max(1, ...days.map(day => day.value)) * 75)}%` }} /><span className="mt-3 text-xs text-gray-500">{d.label}</span></div>)}</div>
                </div>
            </div>
            <Panel><p className="text-sm text-gray-500">Los indicadores se calculan a partir de los pedidos registrados en el servidor. Las ventas corresponden a pedidos completados.</p></Panel>
        </div>
    );
}
