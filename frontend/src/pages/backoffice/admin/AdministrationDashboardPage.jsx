import {Link} from 'react-router-dom';
import {Users,Package,ShoppingCart,Building2,LayoutDashboard} from 'lucide-react';
import {SectionHeader,Panel} from '@/components/shared/ManagementUI';
import {useBackOffice} from '@/components/layout/backoffice/BackOfficeProvider';
const sections=[['workers','Trabajadores',Users,'usuarios'],['products','Productos',Package,'productos'],['services','Servicios',ShoppingCart,'servicios'],['providers','Proveedores',Building2,'proveedores']];
export default function AdministrationDashboardPage(){
 const {data,notes}=useBackOffice();
 return <div className="space-y-6"><SectionHeader icon={LayoutDashboard} title="Resumen administrativo" description="Gestión de trabajadores, productos, servicios y proveedores."/><div className="grid gap-6 sm:grid-cols-2">{sections.map(([key,label,Icon,resource])=><Link key={key} to={'/admin/'+({workers:'trabajadores',products:'productos',services:'servicios',providers:'proveedores'}[key])}><Panel><div className="flex items-center gap-3 text-brand"><Icon size={24}/><h2 className="font-semibold">{label}</h2></div><p className="my-4 text-3xl font-semibold">{notes.some(n=>n.startsWith('No se pudo cargar /admin/'+resource+':'))?'—':data[key].length}</p><p className="text-sm text-gray-500">Consultar {label.toLowerCase()} →</p></Panel></Link>)}</div></div>;
}
