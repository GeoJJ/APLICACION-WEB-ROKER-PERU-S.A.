import { Link } from "react-router-dom";
import { AlertTriangle, ArrowRight, BarChart3, Building2, Package, Plus, Users } from "lucide-react";
import { Panel, SectionHeader } from "@/components/shared/ManagementUI";
import { useBackOffice } from "@/components/layout/backoffice/BackOfficeProvider";

const stockStatus = product => {
    if (product.stock == null || product.umbral == null) return "Sin datos";
    const stock = Number(product.stock);
    const threshold = Number(product.umbral);
    if (!Number.isFinite(stock) || !Number.isFinite(threshold)) return "Sin datos";
    if (stock === 0) return "Agotado";
    if (stock <= threshold / 2) return "Crítico";
    if (stock <= threshold * 1.5) return "Bajo";
    return "Normal";
};
const statusColors = { Normal: "bg-emerald-500", Bajo: "bg-amber-400", Crítico: "bg-orange-500", Agotado: "bg-red-500", "Sin datos": "bg-slate-300" };
const normalize = value => String(value || "Sin categoría").trim();

function Bars({ title, rows, unavailable, subtitle }) {
    const max = Math.max(1, ...rows.map(row => row.count));
    return <Panel><h2 className="text-lg font-semibold text-gray-900">{title}</h2><p className="mt-1 text-sm text-gray-500">{subtitle}</p>
        {unavailable ? <p role="alert" className="mt-6 text-sm text-red-700">No se pudo cargar esta información.</p> : rows.length ? <ul className="mt-6 space-y-4" aria-label={title}>{rows.map(row => <li key={row.label}><div className="mb-1.5 flex items-center justify-between gap-3 text-sm"><span className="min-w-0 break-words text-gray-700">{row.label}</span><span className="font-semibold text-gray-900">{row.count}</span></div><div className="h-2.5 overflow-hidden rounded-full bg-gray-100"><div className={`h-full rounded-full ${row.color || "bg-blue-600"}`} style={{ width: `${row.count / max * 100}%` }} /></div></li>)}</ul> : <p className="mt-6 rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-500">Aún no hay datos disponibles.</p>}
    </Panel>;
}

const chartColors = ["#2563eb", "#06b6d4", "#6366f1", "#14b8a6", "#f59e0b", "#f97316", "#ef4444", "#94a3b8"];

function Donut({ title, subtitle, rows, unavailable }) {
    const total = rows.reduce((sum, row) => sum + row.count, 0);
    const slices = rows.map((row, index) => {
        const start = rows.slice(0, index).reduce((sum, item) => sum + item.count, 0) / total * 100;
        const end = start + row.count / total * 100;
        return `${chartColors[index % chartColors.length]} ${start}% ${end}%`;
    });
    return <Panel><h2 className="text-lg font-semibold text-gray-900">{title}</h2><p className="mt-1 text-sm text-gray-500">{subtitle}</p>
        {unavailable ? <p role="alert" className="mt-6 text-sm text-red-700">No se pudo cargar esta información.</p> : !total ? <p className="mt-6 rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-500">Aún no hay datos disponibles.</p> : <div className="mt-6 flex flex-wrap items-center justify-center gap-6"><div role="img" aria-label={`${title}: ${rows.map(row => `${row.label}, ${row.count}`).join("; ")}`} className="grid h-40 w-40 shrink-0 place-items-center rounded-full" style={{background: `conic-gradient(${slices.join(", ")})`}}><div className="grid h-28 w-28 place-content-center rounded-full bg-white text-center"><span className="text-2xl font-bold text-gray-900">{total}</span><span className="text-xs text-gray-500">productos</span></div></div><ul className="min-w-32 flex-1 space-y-3">{rows.map((row, index) => <li key={row.label} className="flex items-center justify-between gap-3 text-sm"><span className="flex items-center gap-2 text-gray-700"><span className="h-3 w-3 shrink-0 rounded-full" style={{backgroundColor: chartColors[index % chartColors.length]}} />{row.label}</span><span className="font-semibold">{row.count}</span></li>)}</ul></div>}
    </Panel>;
}

function MovementTrend({ movements, unavailable }) {
    const byDate = new Map();
    movements.forEach(movement => { if (movement.fecha && /^\d{4}-\d{2}-\d{2}$/.test(movement.fecha)) byDate.set(movement.fecha, (byDate.get(movement.fecha) || 0) + 1); });
    const rows = [...byDate].sort(([a], [b]) => a.localeCompare(b)).slice(-7);
    const max = Math.max(1, ...rows.map(([, count]) => count));
    const points = rows.map(([, count], index) => ({ x: rows.length === 1 ? 50 : 6 + index * 88 / (rows.length - 1), y: 86 - count / max * 70 }));
    return <Panel><h2 className="text-lg font-semibold text-gray-900">Evolución de movimientos</h2><p className="mt-1 text-sm text-gray-500">Últimas siete fechas con actividad · cantidad de movimientos</p>
        {unavailable ? <p role="alert" className="mt-6 text-sm text-red-700">No se pudo cargar esta información.</p> : !rows.length ? <p className="mt-6 rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-500">Aún no hay movimientos con fecha.</p> : <div className="mt-4"><svg viewBox="0 0 100 100" preserveAspectRatio="none" className="h-40 w-full overflow-visible" role="img" aria-label={`Movimientos: ${rows.map(([date,count]) => `${date}, ${count}`).join("; ")}`}><line x1="5" y1="86" x2="95" y2="86" stroke="#e5e7eb" strokeWidth="0.5" /><polyline points={points.map(point => `${point.x},${point.y}`).join(" ")} fill="none" stroke="#2563eb" strokeWidth="1.4" vectorEffect="non-scaling-stroke" />{points.map((point, index) => <circle key={rows[index][0]} cx={point.x} cy={point.y} r="1.5" fill="#2563eb" />)}</svg><ul className="mt-3 flex flex-wrap justify-between gap-2 text-xs text-gray-500">{rows.map(([date,count]) => <li key={date}>{date.slice(8,10)}/{date.slice(5,7)}/{date.slice(0,4)}: <strong className="text-gray-700">{count}</strong></li>)}</ul></div>}
    </Panel>;
}

function DataPanel({ title, subtitle, columns, rows, unavailable, empty, to }) {
    return <Panel><div className="flex flex-wrap items-start justify-between gap-3"><div><h2 className="text-lg font-semibold text-gray-900">{title}</h2><p className="mt-1 text-sm text-gray-500">{subtitle}</p></div><Link to={to} className="inline-flex items-center gap-1 text-sm font-semibold text-brand hover:underline">Ver todo <ArrowRight size={16} aria-hidden="true" /></Link></div>
        {unavailable ? <p role="alert" className="mt-6 text-sm text-red-700">No se pudo cargar esta información.</p> : rows.length ? <div className="mt-5 overflow-x-auto"><table className="w-full min-w-[460px] text-left text-sm"><thead className="border-b border-gray-200 text-xs uppercase text-gray-500"><tr>{columns.map(column => <th key={column.label} scope="col" className="px-3 py-3 font-semibold">{column.label}</th>)}</tr></thead><tbody>{rows.map((row, index) => <tr key={row.id ?? index} className="border-b border-gray-100 last:border-0">{columns.map(column => <td key={column.label} className="px-3 py-3 text-gray-700">{column.render(row)}</td>)}</tr>)}</tbody></table></div> : <p className="mt-6 rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-500">{empty}</p>}
    </Panel>;
}

export default function AdministrationDashboardPage() {
    const { data, notes, refresh } = useBackOffice();
    const failed = resource => notes.some(note => note.startsWith(`No se pudo cargar /admin/${resource}:`));
    const productsUnavailable = failed("productos");
    const movementsUnavailable = failed("inventario/movimientos");
    const alertsUnavailable = failed("inventario/alertas");
    const categories = new Map();
    data.products.forEach(product => { const label = normalize(product.categoria); categories.set(label, (categories.get(label) || 0) + 1); });
    const categoryRows = [...categories].sort((a, b) => b[1] - a[1]).map(([label, count]) => ({ label, count }));
    const statuses = ["Normal", "Bajo", "Crítico", "Agotado", "Sin datos"].map(label => ({ label, count: data.products.filter(product => stockStatus(product) === label).length, color: statusColors[label] })).filter(row => row.count);
    const movementRows = ["INGRESO", "SALIDA", "AJUSTE"].map(type => ({ label: type.charAt(0) + type.slice(1).toLowerCase(), count: data.movements.filter(m => String(m.tipo).toUpperCase() === type).length }));
    const alerts = data.alerts.filter(alert => !String(alert.nivel || "").startsWith("NINGUNA"));
    const stats = [
        { label: "Productos", value: data.products.length, unavailable: productsUnavailable, icon: Package, to: "/admin/productos", caption: "En catálogo" },
        { label: "Trabajadores", value: data.workers.filter(worker => worker.nombreRol !== "CLIENTE").length, unavailable: failed("usuarios"), icon: Users, to: "/admin/trabajadores", caption: "Cuentas de trabajadores" },
        { label: "Proveedores", value: data.providers.length, unavailable: failed("proveedores"), icon: Building2, to: "/admin/proveedores", caption: "Registrados" },
        { label: "Alertas de stock", value: alerts.length, unavailable: alertsUnavailable, icon: AlertTriangle, to: "/admin/inventario", caption: "Requieren revisión" },
    ];
    const stockColumns = [
        { label: "Producto", render: row => row.producto || "—" },
        { label: "Stock", render: row => row.stock ?? "—" },
        { label: "Umbral", render: row => row.umbral ?? "—" },
        { label: "Estado", render: row => <span className="inline-block rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-800">{String(row.nivel || "Sin datos").split(" - ")[0]}</span> },
    ];
    const movementColumns = [
        { label: "Fecha", render: row => row.fecha ? new Date(`${row.fecha}T00:00:00`).toLocaleDateString("es-PE") : "—" },
        { label: "Producto", render: row => row.producto || "—" },
        { label: "Tipo", render: row => row.tipo || "—" },
        { label: "Cantidad", render: row => row.cantidad ?? "—" },
    ];
    return <div className="space-y-6"><SectionHeader icon={BarChart3} title="Resumen administrativo" description="Supervisa los recursos y el estado del inventario desde un solo lugar." />
        {notes.some(note => note.startsWith("No se pudo cargar /admin/")) && <div role="alert" className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900"><span>Algunos datos no están disponibles. Puedes volver a consultarlos.</span><button type="button" onClick={refresh} className="font-semibold underline underline-offset-4">Reintentar</button></div>}
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{stats.map(({ label, value, unavailable, icon: Icon, to, caption }) => <Link key={label} to={to} className="group rounded-2xl border border-gray-200 bg-white p-5 shadow-sm transition hover:border-blue-200 hover:shadow-md"><div className="flex items-center justify-between gap-3"><span className="text-sm font-medium text-gray-600">{label}</span><span className="rounded-xl bg-blue-50 p-2.5 text-brand"><Icon size={21} aria-hidden="true" /></span></div><p className="mt-4 text-3xl font-bold text-gray-900">{unavailable ? "—" : value}</p><p className="mt-2 text-xs text-gray-500">{unavailable ? "No disponible" : caption}</p></Link>)}</div>
        <div className="grid gap-6 xl:grid-cols-3"><Donut title="Productos por categoría" subtitle="Cantidad de productos registrados" rows={categoryRows} unavailable={productsUnavailable} /><Donut title="Estado del inventario" subtitle="Stock respecto al umbral de cada producto" rows={statuses} unavailable={productsUnavailable} /><Bars title="Movimientos de inventario" subtitle="Cantidad de movimientos por tipo" rows={data.movements.length ? movementRows : []} unavailable={movementsUnavailable} /></div>
        <MovementTrend movements={data.movements} unavailable={movementsUnavailable} />
        <div className="grid gap-6 2xl:grid-cols-2"><DataPanel title="Productos por reponer" subtitle="Alertas de inventario que requieren revisión" columns={stockColumns} rows={alerts.slice(0, 6)} unavailable={alertsUnavailable} empty="No hay alertas de stock en este momento." to="/admin/inventario" /><DataPanel title="Últimos movimientos" subtitle="Actividad reciente de inventario" columns={movementColumns} rows={data.movements.slice(0, 6)} unavailable={movementsUnavailable} empty="Aún no hay movimientos registrados." to="/admin/inventario" /></div>
        <Panel><h2 className="text-lg font-semibold text-gray-900">Accesos rápidos</h2><div className="mt-4 flex flex-wrap gap-3">{[["Productos", "/admin/productos"], ["Trabajadores", "/admin/trabajadores"], ["Inventario", "/admin/inventario"], ["Servicios", "/admin/servicios"]].map(([label, to]) => <Link key={to} to={to} className="inline-flex items-center gap-2 rounded-xl border border-blue-100 bg-blue-50 px-4 py-3 text-sm font-semibold text-brand transition hover:bg-blue-100"><Plus size={16} aria-hidden="true" />{label}</Link>)}</div></Panel>
    </div>;
}
