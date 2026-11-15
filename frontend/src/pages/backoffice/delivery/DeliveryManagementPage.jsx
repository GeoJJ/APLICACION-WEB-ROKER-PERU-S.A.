import { useState } from 'react';
import { Truck, FileText } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { localDate } from '@/data/backofficeStore';
import { SectionHeader, Panel, SearchBox, Modal, Field } from '@/components/shared/ManagementUI';
import SignaturePad from '@/components/features/delivery/SignaturePad';
import DataTable from '@/components/shared/DataTable';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
export default function DeliveryManagementPage({ history = false }) {
 const { data, update, setNotice, readOnly } = useBackOffice();
 const [tab, setTab] = useState('hoy');
 const [search, setSearch] = useState('');
 const [draft, setDraft] = useState(null);
 const [error, setError] = useState('');
 const [date, setDate] = useState('');
 const finished=history || Boolean(draft && data.deliveries.some(d=>d.id===draft.id && ['Entregado','Cancelado'].includes(d.estado)));
 const groups = { hoy: data.deliveries.filter(d => d.estado !== 'Cancelado'), pendientes: data.deliveries.filter(d => ['Pendiente', 'En camino'].includes(d.estado)), completados: data.deliveries.filter(d => d.estado === 'Entregado') };
 const rows = (history ? data.deliveries.filter(d => ['Entregado', 'Cancelado'].includes(d.estado)) : groups[tab]).filter(d => `${d.id} ${d.cliente}`.toLowerCase().includes(search.toLowerCase()) && (!date || d.fecha === date));
 async function save(e) {
  e.preventDefault();
  if (draft.estado === 'Entregado' && (!draft.evidencia || !draft.firma)) { setError('Para confirmar la entrega, adjunta una foto y la firma del cliente.'); return; }
  if (await update(old => ({ ...old, deliveries: old.deliveries.map(d => d.id === draft.id ? { ...draft, fechaEntrega: draft.estado === 'Entregado' ? localDate() : '' } : d), orders: old.orders.map(o => o.id === draft.id ? { ...o, estado: draft.estado === 'Entregado' ? 'Completado' : draft.estado } : o) }))) setDraft(null);
 }
 function photo(e) {
  const file = e.target.files[0];
  if (!file) return;
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 1500000) { setError('Selecciona una imagen JPG, PNG o WebP de hasta 1,5 MB.'); return; }
  const reader = new FileReader();
  reader.onload = () => { setDraft(current => ({ ...current, evidencia: reader.result })); setError(''); };
  reader.onerror = () => setNotice('No se pudo leer la imagen.');
  reader.readAsDataURL(file);
 }
 return <><SectionHeader icon={history ? FileText : Truck} title={history ? 'Historial de Entregas' : 'Mis Entregas Asignadas'} description={history ? 'Consulta las entregas finalizadas y sus evidencias.' : 'Revisa los pedidos y registra el resultado de cada entrega.'} /><Panel>{!history && <div className="mb-6 flex flex-wrap gap-3">{[['hoy', 'Asignadas'], ['pendientes', 'Pendientes'], ['completados', 'Completados']].map(([id, label]) => <Button key={id} size="sm" variant={tab === id ? 'primary' : 'outline'} onClick={() => setTab(id)}>{label} ({groups[id].length})</Button>)}</div>}<div className="mb-6 flex flex-wrap items-end gap-3"><SearchBox value={search} onChange={setSearch} placeholder="Buscar pedido o cliente..." /><Field label="Fecha informada por la API"><input className="bo-input" type="date" value={date} onChange={e => setDate(e.target.value)} /></Field></div><DataTable rows={rows} columns={[{ key: 'id', label: 'Pedido' }, { key: 'cliente', label: 'Cliente' }, { key: 'direccion', label: 'Dirección' }, { key: 'estado', label: 'Estado', render: d => <Badge variant={d.estado === 'Entregado' ? 'success' : d.estado === 'Cancelado' ? 'danger' : 'info'}>{d.estado}</Badge> }, { key: 'accion', label: 'Acción', render: d => <Button size="sm" variant="outline" onClick={() => { setError(''); setDraft({ ...d }); }}>Ver</Button> }]} /></Panel>{draft && <Modal title={`Entrega ${draft.id}`} onClose={() => setDraft(null)}><form onSubmit={save}><div className="mb-6 rounded-xl bg-gray-50 p-4 text-sm leading-7"><p className="font-semibold">{draft.cliente}</p><p>{draft.direccion}</p><a className="text-brand underline" href={`tel:${draft.telefono.replaceAll(' ', '')}`}>{draft.telefono}</a>{draft.fechaEntrega && <p>Entregado: {draft.fechaEntrega}</p>}</div><div className="grid gap-5"><Field label="Estado"><select disabled={finished} className="bo-input" value={draft.estado} onChange={e => setDraft({ ...draft, estado: e.target.value })}>{['En camino', 'Entregado'].map(s => <option key={s}>{s}</option>)}</select></Field><Field label="Observaciones"><textarea readOnly={finished} className="bo-input" rows={3} value={draft.observaciones || ''} onChange={e => setDraft({ ...draft, observaciones: e.target.value })} /></Field><Field label="Evidencia de entrega">{!finished && <input type="file" accept="image/jpeg,image/png,image/webp" capture="environment" onChange={photo} className="bo-input" />}{draft.evidencia ? <img src={draft.evidencia} alt="Evidencia de entrega" className="max-h-60 w-full rounded-lg object-contain" /> : <p className="text-sm text-gray-500">Sin fotografía adjunta.</p>}</Field><Field label="Firma del cliente">{finished ? draft.firma ? <img src={draft.firma} alt="Firma del cliente" className="h-32 w-full object-contain" /> : <p>Sin firma registrada.</p> : <><SignaturePad value={draft.firma} onChange={firma => setDraft({ ...draft, firma })} />{draft.firma && <img src={draft.firma} alt="Firma registrada" className="h-20 object-contain" />}</>}</Field>{error && <p role="alert" className="text-sm text-red-700">{error}</p>}{!finished && <Button type="submit" disabled={readOnly}>Guardar entrega</Button>}</div></form></Modal>}</>;
}
