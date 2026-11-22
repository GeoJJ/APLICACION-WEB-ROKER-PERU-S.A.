import {Link} from 'react-router-dom';
import {Users,Package,ShoppingCart,Building2,UserCog,ShieldCheck,Boxes,LayoutDashboard} from 'lucide-react';
import {SectionHeader,Panel} from '@/components/shared/ManagementUI';
import {useBackOffice} from '@/components/layout/backoffice/BackOfficeProvider';
// [clave en data, título, icono, ruta del controller (para detectar si falló la carga), ruta en el panel, texto del enlace]
const sections=[
 ['workers','Trabajadores',Users,'usuarios','/admin/trabajadores'],
 ['products','Productos',Package,'productos','/admin/productos'],
 ['services','Servicios',ShoppingCart,'servicios','/admin/servicios'],
 ['providers','Proveedores',Building2,'proveedores','/admin/proveedores'],
 ['employees','Personal de servicio',UserCog,'empleados-servicio','/admin/personal'],
 ['roles','Roles',ShieldCheck,'roles','/admin/roles'],
 ['alerts','Alertas de stock',Boxes,'inventario/alertas','/admin/inventario'],
];
export default function AdministrationDashboardPage(){
 const {data,notes}=useBackOffice();
 const count=(key,resource)=>notes.some(n=>n.startsWith('No se pudo cargar /admin/'+resource+':'))?'—':key==='alerts'?data.alerts.filter(a=>!a.nivel.startsWith('NINGUNA')).length:data[key].length;
 return <div className="space-y-6"><SectionHeader icon={LayoutDashboard} title="Resumen administrativo" description="Gestión de trabajadores, productos, servicios, proveedores, personal, roles e inventario."/><div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">{sections.map(([key,label,Icon,resource,to])=><Link key={key} to={to}><Panel><div className="flex items-center gap-3 text-brand"><Icon size={24}/><h2 className="font-semibold">{label}</h2></div><p className="my-4 text-3xl font-semibold">{count(key,resource)}</p><p className="text-sm text-gray-500">Consultar {label.toLowerCase()} →</p></Panel></Link>)}</div></div>;
}
