import { useState } from 'react';
import { Users, Package, ShoppingCart, Plus, Pencil } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { ROLES } from '@/data/roles';
import { money, stockState } from '@/data/backofficeStore';
import { SectionHeader, Panel, Field, Modal, FormActions, SearchBox } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
const configs = {
 providers: { title:'Gestión de Proveedores', singular:'proveedor', icon:Package, fields:[['nombre','Proveedor'],['ruc','RUC (11 dígitos)'],['telefono','Teléfono','tel'],['email','Correo','email']] },
 workers: { title: 'Gestión de Trabajadores', singular: 'trabajador', icon: Users, fields: [['nombre', 'Nombre completo'], ['email', 'Correo electrónico', 'email'], ['telefono', 'Teléfono', 'tel'], ['documento','Documento'], ['direccion','Dirección'], ['rol', 'Rol', 'role'], ['estado', 'Estado', 'status']] },
 products: { title: 'Gestión de Productos', singular: 'producto', icon: Package, fields: [['codigo','Código'], ['producto', 'Producto'], ['categoria', 'Categoría'], ['precio', 'Precio (S/)', 'number'], ['umbral', 'Umbral de stock', 'number'], ['idProveedor','Proveedor','provider'], ['estado', 'Estado', 'status']] },
 services: { title: 'Gestión de Servicios', singular: 'servicio', icon: ShoppingCart, fields: [['categoria','Categoría'], ['nombre', 'Servicio'], ['descripcion', 'Descripción'], ['precio', 'Tarifa (S/)', 'number'], ['duracion','Duración estimada'], ['personal','Personal requerido','number'], ['estado', 'Estado', 'status']] },
};
export default function ManagementPage({ kind, readonly = false }) {
 const { data, update } = useBackOffice();
 const config = configs[kind];
 const [search, setSearch] = useState('');
 const [filter, setFilter] = useState('');
 const [draft, setDraft] = useState(null);
 const roles = ROLES.filter(r => r.id !== 'customer');
 const options = type => type === 'provider' ? data.providers.map(p => [p.id,p.nombre]) : type === 'role' ? roles.map(r => [r.id, r.label]) : [['Activo', 'Activo'], ['Inactivo', 'Inactivo']];
 const columns = config.fields.filter(([key])=>!({workers:['documento','direccion'],products:['idProveedor'],services:['descripcion','duracion']}[kind]||[]).includes(key)).map(([key, label, type]) => ({ key, label, render: r => type === 'provider' ? data.providers.find(p => String(p.id) === String(r.idProveedor))?.nombre : type === 'role' ? ROLES.find(role => role.id === r.rol)?.label : type === 'status' ? <Badge variant={r.estado === 'Activo' ? 'success' : 'warning'}>{r.estado}</Badge> : key === 'precio' ? money(r[key]) : r[key] }));
 if (kind === 'products') columns.push({ key: 'stock', label: 'Stock', render: r => <>{r.stock}<p className="text-xs text-gray-500">{stockState(r)}</p></> });
 if (!readonly) columns.push({ key: 'action', label: 'Acción', render: r => <Button size="sm" variant="outline" onClick={() => setDraft({ ...r })}><Pencil size={15} className="mr-2" />Editar</Button> });
 async function save(e) {
  e.preventDefault();
  const record = { ...draft };
  config.fields.forEach(([key, , type]) => { record[key] = type === 'number' ? Number(record[key]) : String(record[key] ?? '').trim(); });
  if (kind === 'workers' && data.workers.some(w => w.id !== record.id && w.email.toLowerCase() === record.email.toLowerCase())) { e.currentTarget.elements.email.setCustomValidity('Este correo ya está registrado.'); e.currentTarget.reportValidity(); return; }
  if (await update(old => ({ ...old, [kind]: record.id ? old[kind].map(r => r.id === record.id ? record : r) : [...old[kind], { ...record, id: crypto.randomUUID(), ...(kind === 'products' ? { stock: 0 } : {}) }] }))) setDraft(null);
 }
 return <><SectionHeader icon={config.icon} title={readonly ? 'Consulta de Productos' : config.title} description="Consulta y administra la información del sistema." action={!readonly && <Button onClick={() => setDraft({ estado: 'Activo', rol: 'seller', tipoPersona:'NATURAL', personal:1 })}><Plus size={18} className="mr-2" />Nuevo {config.singular}</Button>} /><Panel><div className="mb-6 flex flex-wrap gap-3"><SearchBox value={search} onChange={setSearch} placeholder={`Buscar ${config.singular}...`} />{kind!=='providers' && <select aria-label="Filtrar" className="bo-input sm:max-w-56" value={filter} onChange={e => setFilter(e.target.value)}><option value="">{kind === 'workers' ? 'Todos los roles' : 'Todos los estados'}</option>{options(kind === 'workers' ? 'role' : 'status').map(([id, label]) => <option key={id} value={id}>{label}</option>)}</select>}</div><DataTable columns={columns} rows={data[kind].filter(r => Object.values(r).join(' ').toLowerCase().includes(search.toLowerCase()) && (!filter || (kind === 'workers' ? r.rol : r.estado) === filter))} /></Panel>
 {draft && <Modal title={`${draft.id ? 'Editar' : 'Nuevo'} ${config.singular}`} onClose={() => setDraft(null)}><form onSubmit={save}><div className="grid gap-4 sm:grid-cols-2">{config.fields.map(([key, label, type]) => <Field key={key} label={label}>{['role','status','provider'].includes(type) ? <select required className="bo-input" value={draft[key] || ''} onChange={e => setDraft({ ...draft, [key]: e.target.value })}><option value="">Seleccionar</option>{options(type).map(([id, text]) => <option key={id} value={id}>{text}</option>)}</select> : <input name={key} required={!(draft.id && ['telefono','direccion'].includes(key))} className="bo-input" type={type || 'text'} min={key==='personal'?1:0} step={key === 'precio' ? '0.01' : '1'} pattern={type === 'tel' ? '[0-9]{9}' : key === 'ruc' ? '[0-9]{11}' : key === 'documento' ? '[0-9]{8}' : undefined} value={draft[key] ?? ''} onChange={e => { e.target.setCustomValidity(''); setDraft({ ...draft, [key]: e.target.value }); }} />}</Field>)}</div>{kind === 'workers' && <div className="mt-4"><Field label={draft.id ? 'Nueva contraseña (opcional)' : 'Contraseña inicial'}><input className="bo-input" type="password" autoComplete="new-password" minLength={8} maxLength={72} required={!draft.id} value={draft.password || ''} onChange={e=>setDraft({...draft,password:e.target.value})} /></Field></div>}<FormActions onClose={() => setDraft(null)} /></form></Modal>}</>;
}
