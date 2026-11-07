import { Outlet } from "react-router-dom";
import BackOfficeHeader from "./BackOfficeHeader";
import BackOfficeSidebar from "./BackOfficeSidebar";
import PublicFooter from '../storefront/PublicFooter';
import { useBackOffice } from './BackOfficeProvider';
import { ROLES } from '@/data/roles';

export default function BackOfficeLayout({ role = "manager" }) {
    const roleName = ROLES.find(item => item.id === role)?.label;
    const { notice, setNotice, loading, error, refresh, notes, actorId, setActorId } = useBackOffice();

    return (
        <div className="min-h-screen bg-gray-50">
            <BackOfficeHeader role={roleName} />
            <BackOfficeSidebar role={role} />
            <main className="min-h-[calc(100vh-4rem)] md:ml-64 md:pt-12">
                <div className="p-4 md:p-6 max-w-7xl mx-auto">
                    <div role="status" className="mb-5 rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-brand">Vista previa del panel. La autenticación y las acciones de edición están pendientes de integración con el backend actual.</div>{role === 'delivery' && <label className="mb-5 grid max-w-sm gap-2 text-sm font-medium">ID del repartidor para consultar<input className="bo-input" type="number" min="1" value={actorId} onChange={e=>setActorId(e.target.value)} /></label>}{notes.map(note=><p key={note} className="mb-4 rounded-lg bg-white p-4 text-sm leading-6 text-gray-600">{note}</p>)}{notice && <div role="status" className="mb-5 flex items-center justify-between gap-4 rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm text-brand">{notice}<button onClick={() => setNotice('')} aria-label="Cerrar aviso">✕</button></div>}
                    {loading ? <p className="p-8 text-gray-500">Cargando información del servidor...</p> : error ? <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-6"><p>{error}</p><button className="mt-3 text-brand underline" onClick={refresh}>Reintentar</button></div> : <Outlet />}
                </div>
            </main>
            <div className="md:ml-64"><PublicFooter /></div>
        </div>
    );
}
