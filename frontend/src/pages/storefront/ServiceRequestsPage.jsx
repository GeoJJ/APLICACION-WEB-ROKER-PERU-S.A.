import {useState} from 'react';
import {Link} from 'react-router-dom';
import {ClipboardList} from 'lucide-react';
import {Panel,Field,SectionHeader} from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';
export default function ServiceRequestsPage({management=false}){
 const [quote,setQuote]=useState({precio:'',programada:'',observaciones:''});
 const content=<><SectionHeader icon={ClipboardList} title={management?'Solicitudes de servicios':'Mis solicitudes'} description={management?'Revisión de necesidades y preparación de cotizaciones.':'Seguimiento de tus solicitudes de servicios.'}/><Panel><p role="status" className="rounded-xl bg-blue-50 p-4 text-sm leading-6 text-brand">El backend de esta rama todavía no dispone del flujo de solicitudes y cotizaciones. Esta vista no crea ni modifica registros.</p><div className="py-12 text-center text-gray-500">No hay solicitudes disponibles para consultar.</div>{!management&&<Link to="/servicios" className="font-semibold text-brand">Explorar servicios</Link>}</Panel>{management&&<Panel className="mt-6"><h2 className="mb-5 text-lg font-semibold">Formulario de cotización</h2><div className="grid gap-5 sm:grid-cols-2"><Field label="Precio cotizado (S/)"><input className="bo-input" type="number" min="0.01" step="0.01" value={quote.precio} onChange={e=>setQuote({...quote,precio:e.target.value})}/></Field><Field label="Fecha y hora disponibles"><input className="bo-input" type="datetime-local" value={quote.programada} onChange={e=>setQuote({...quote,programada:e.target.value})}/></Field></div><div className="mt-5"><Field label="Detalle de la propuesta"><textarea className="bo-input" rows={3} value={quote.observaciones} onChange={e=>setQuote({...quote,observaciones:e.target.value})}/></Field></div><Button className="mt-5" disabled>Enviar cotización</Button></Panel>}</>;
 return management?content:<div className="mx-auto max-w-[1100px] px-4 py-10 md:px-6">{content}</div>;
}
