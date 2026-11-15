import {BarChart3} from 'lucide-react';
import {SectionHeader,Panel,Summary} from '@/components/shared/ManagementUI';
import {useBackOffice} from '@/components/layout/backoffice/BackOfficeProvider';
export default function AdminDashboardPage(){
 const {data,notes}=useBackOffice();const unavailable=notes.some(n=>n.startsWith('No se pudo'));
 return <div className="space-y-6"><SectionHeader icon={BarChart3} title="Resumen gerencial" description="Consulta de información disponible para Gerente General."/><Summary items={[["Movimientos de inventario",unavailable?'—':data.movements.length],['Ventas','No disponible'],['Pedidos','No disponible'],['Clientes','No disponible']]}/><div className="grid gap-6 lg:grid-cols-2">{['Ventas por categoría','Ventas de los últimos 7 días'].map(title=><Panel key={title}><h2 className="font-semibold">{title}</h2><div className="mt-5 flex min-h-56 items-center justify-center rounded-xl bg-gray-50 p-8 text-center text-sm leading-6 text-gray-500">La API de Gerente General aún no proporciona datos de ventas para este gráfico.</div></Panel>)}</div></div>;
}
