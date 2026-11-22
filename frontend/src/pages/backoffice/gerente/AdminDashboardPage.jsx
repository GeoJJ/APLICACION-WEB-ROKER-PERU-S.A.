import { Link } from 'react-router-dom';
import { BarChart3, FileText, Package } from 'lucide-react';
import { SectionHeader, Panel, Summary } from '@/components/shared/ManagementUI';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';

// Resumen gerencial: supervisar la operación (movimientos de inventario) y consultar información general del negocio.
export default function AdminDashboardPage() {
    const { data, notes, loading } = useBackOffice();
    const unavailable = notes.some(n => n.startsWith('No se pudo'));
    const movimientos = data.movements;

    const cuenta = tipo => movimientos.filter(m => String(m.tipo).toUpperCase() === tipo).length;
    const dato = valor => (unavailable ? '—' : loading ? '…' : valor);

    // Productos con más movimientos (top 5)
    const porProducto = {};
    movimientos.forEach(m => { porProducto[m.producto] = (porProducto[m.producto] || 0) + 1; });
    const topProductos = Object.entries(porProducto).sort((a, b) => b[1] - a[1]).slice(0, 5);

    const ultimos = movimientos.slice(0, 8); // el backend los entrega del más reciente al más antiguo

    return (
        <div className="space-y-6">
            <SectionHeader
                icon={BarChart3}
                title="Resumen gerencial"
                description="Supervisa la operación y consulta la información general del negocio."
            />

            <Summary items={[
                ['Movimientos de inventario', dato(movimientos.length)],
                ['Ingresos', dato(cuenta('INGRESO'))],
                ['Salidas', dato(cuenta('SALIDA'))],
                ['Ajustes', dato(cuenta('AJUSTE'))],
            ]} />

            <div className="grid gap-6 lg:grid-cols-3">
                <Panel className="lg:col-span-2">
                    <h2 className="font-semibold">Últimos movimientos de inventario</h2>
                    {ultimos.length === 0 ? (
                        <p className="mt-5 rounded-xl bg-gray-50 p-8 text-center text-sm text-gray-500">
                            {unavailable ? 'No se pudo cargar la información.' : loading ? 'Cargando…' : 'Aún no hay movimientos registrados.'}
                        </p>
                    ) : (
                        <div className="mt-4 overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead className="text-xs uppercase text-gray-500">
                                    <tr>
                                        <th className="py-2 pr-4">Fecha</th>
                                        <th className="py-2 pr-4">Producto</th>
                                        <th className="py-2 pr-4">Tipo</th>
                                        <th className="py-2 text-right">Cantidad</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-100">
                                    {ultimos.map(m => (
                                        <tr key={m.id}>
                                            <td className="py-2 pr-4 text-gray-500">{m.fecha}</td>
                                            <td className="py-2 pr-4 font-medium text-gray-800">{m.producto}</td>
                                            <td className="py-2 pr-4">{m.tipo}</td>
                                            <td className="py-2 text-right font-semibold">{m.cantidad}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Panel>

                <div className="space-y-6">
                    <Panel>
                        <h2 className="font-semibold">Productos con más movimiento</h2>
                        {topProductos.length === 0 ? (
                            <p className="mt-4 text-sm text-gray-500">Sin datos todavía.</p>
                        ) : (
                            <ol className="mt-4 space-y-2 text-sm">
                                {topProductos.map(([nombre, veces]) => (
                                    <li key={nombre} className="flex justify-between gap-3">
                                        <span className="truncate text-gray-700">{nombre}</span>
                                        <span className="shrink-0 font-semibold text-brand">{veces}</span>
                                    </li>
                                ))}
                            </ol>
                        )}
                    </Panel>

                    <Panel>
                        <h2 className="font-semibold">Accesos rápidos</h2>
                        <div className="mt-4 grid gap-2 text-sm font-medium">
                            <Link to="/manager/inventario" className="flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 hover:bg-gray-50">
                                <Package size={18} className="text-brand" /> Ver inventario
                            </Link>
                            <Link to="/manager/reportes" className="flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 hover:bg-gray-50">
                                <FileText size={18} className="text-brand" /> Reportes de ventas e inventario
                            </Link>
                        </div>
                    </Panel>
                </div>
            </div>
        </div>
    );
}