import { useState } from 'react';
import { LogIn, Plus } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { localDate } from '@/data/backofficeStore';
import { SectionHeader, Panel, Modal, Field, FormActions, SearchBox } from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';
import DataTable from '@/components/shared/DataTable';
export default function StockEntriesPage() {
 const { data, update } = useBackOffice();
 const [draft, setDraft] = useState(null);
 const [search, setSearch] = useState('');
 async function save(e) {
  e.preventDefault();
  const product = data.products.find(p => String(p.id) === draft.productId);
  if (!product) return;
  const cantidad = Number(draft.cantidad);
  if (await update(old => ({ ...old, products: old.products.map(p => p.id === product.id ? { ...p, stock: p.stock + cantidad } : p), movements: [{ ...draft, id: crypto.randomUUID(), producto: product.producto, cantidad }, ...old.movements] }))) setDraft(null);
 }
 return <><SectionHeader icon={LogIn} title="Ingresos de Almacén" description="Registra la recepción de mercadería y consulta los movimientos de inventario." action={<Button onClick={() => setDraft({ productId: '', cantidad: '', fecha: localDate(), proveedor: '', observaciones: '' })}><Plus size={18} className="mr-2" />Registrar ingreso</Button>} /><Panel><div className="mb-6"><SearchBox value={search} onChange={setSearch} placeholder="Buscar producto o proveedor..." /></div><DataTable rows={data.movements.filter(m => `${m.producto} ${m.proveedor}`.toLowerCase().includes(search.toLowerCase()))} columns={[{ key: 'fecha', label: 'Fecha' }, { key: 'producto', label: 'Producto' }, { key: 'proveedor', label: 'Proveedor' }, { key: 'cantidad', label: 'Unidades' }, { key: 'observaciones', label: 'Observaciones' }]} emptyText="Todavía no hay ingresos registrados." /></Panel>{draft && <Modal title="Registrar ingreso" onClose={() => setDraft(null)}><form onSubmit={save}><div className="grid gap-4"><Field label="Producto"><select required className="bo-input" value={draft.productId} onChange={e => setDraft({ ...draft, productId: e.target.value })}><option value="">Seleccionar producto</option>{data.products.filter(p => p.estado === 'Activo').map(p => <option key={p.id} value={p.id}>{p.producto}</option>)}</select></Field>{[['cantidad', 'Cantidad recibida', 'number'], ['proveedor', 'Proveedor', 'text'], ['fecha', 'Fecha de ingreso', 'date']].map(([key, label, type]) => <Field label={label} key={key}><input className="bo-input" required type={type} min="1" step="1" max={type === 'date' ? localDate() : undefined} value={draft[key]} onChange={e => setDraft({ ...draft, [key]: e.target.value })} /></Field>)}<Field label="Observaciones"><textarea className="bo-input" value={draft.observaciones} onChange={e => setDraft({ ...draft, observaciones: e.target.value })} /></Field></div><FormActions onClose={() => setDraft(null)} /></form></Modal>}</>;
}
