import { useState } from 'react';
import { Package, AlertTriangle } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { stockState } from '@/data/backofficeStore';
import { SectionHeader, Panel, SearchBox, Summary } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Badge from '@/components/ui/Badge';
export default function StockOverviewPage() {
 const { data } = useBackOffice();
 const [search, setSearch] = useState('');
 const alerts = data.products.filter(p => p.stock < p.umbral);
 return <><SectionHeader icon={Package} title="Stock de Productos" description="Disponibilidad de inventario y alertas de reposición." /><Summary items={[["Productos", data.products.length], ['Unidades disponibles', data.products.reduce((s, p) => s + p.stock, 0)], ['Stock bajo', alerts.length], ['Ingresos registrados', data.movements.length]]} /><Panel><div className="mb-6"><SearchBox value={search} onChange={setSearch} placeholder="Buscar producto..." /></div><DataTable rows={data.products.filter(p => p.producto.toLowerCase().includes(search.toLowerCase()))} columns={[{ key: 'producto', label: 'Producto' }, { key: 'categoria', label: 'Categoría' }, { key: 'stock', label: 'Stock' }, { key: 'umbral', label: 'Umbral' }, { key: 'estado', label: 'Estado', render: p => <Badge variant={stockState(p) === 'En Stock' ? 'success' : stockState(p) === 'Crítico' ? 'danger' : 'warning'}>{stockState(p)}</Badge> }]} /></Panel><h2 className="mb-4 mt-8 text-lg font-semibold">Alertas de Stock Bajo</h2><div className="grid gap-3">{alerts.length ? alerts.map(p => <Panel key={p.id}><div className="flex items-center gap-3 text-sm"><AlertTriangle className="shrink-0 text-amber-500" size={20} /><span>{p.producto}: quedan <strong>{p.stock}</strong> unidades. Umbral: {p.umbral}.</span></div></Panel>) : <Panel>Todos los productos tienen stock suficiente.</Panel>}</div></>;
}
