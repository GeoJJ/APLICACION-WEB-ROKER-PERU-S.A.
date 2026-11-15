import { useState } from 'react';
import { Bell } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { useAuth } from './AuthProvider';
export default function NotificationMenu() {
 const {data}=useBackOffice();const {role}=useAuth();const [open,setOpen]=useState(false);
 const messages=role?.id==='delivery'?data.deliveries.filter(d=>d.estado==='En camino').map(d=>`Entrega pendiente: ${d.id}`):role?.id==='staff'?data.orders.filter(o=>o.estado==='En camino').map(o=>`Servicio asignado: ${o.concepto}`):role?.id==='seller'?data.orders.filter(o=>o.estado==='Pendiente').map(o=>`Pedido por gestionar: ${o.id}`):data.products.filter(p=>p.stock<p.umbral).map(p=>`Stock bajo: ${p.producto} (${p.stock})`);
 return <div className="relative"><button aria-label="Notificaciones" aria-expanded={open} onClick={()=>setOpen(!open)} className="relative rounded-full p-2 text-gray-600 hover:bg-gray-100"><Bell size={20} />{messages.length>0&&<span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-red-500" />}</button>{open&&<div className="absolute right-0 z-50 mt-3 max-h-80 w-64 overflow-auto rounded-xl border border-gray-200 bg-white p-4 shadow-lg"><div className="mb-3 flex justify-between text-sm font-semibold"><span>Notificaciones</span><button aria-label="Cerrar notificaciones" onClick={()=>setOpen(false)}>✕</button></div>{messages.length?messages.map(text=><p key={text} className="border-t border-gray-100 py-3 text-xs text-gray-600">{text}</p>):<p className="text-sm text-gray-500">No tienes notificaciones pendientes.</p>}</div>}</div>;
}
