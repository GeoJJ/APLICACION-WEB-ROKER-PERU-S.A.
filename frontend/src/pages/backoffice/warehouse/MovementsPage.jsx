import {ClipboardList} from 'lucide-react';
import {useBackOffice} from '@/components/layout/backoffice/BackOfficeProvider';
import {SectionHeader,Panel} from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
export default function MovementsPage(){const {data}=useBackOffice();return <><SectionHeader icon={ClipboardList} title="Movimientos de inventario" description="Historial disponible para Gerente General."/><Panel><DataTable rows={data.movements} columns={[{key:'fecha',label:'Fecha'},{key:'producto',label:'Producto'},{key:'tipo',label:'Tipo'},{key:'cantidad',label:'Cantidad'}]}/></Panel></>;}
