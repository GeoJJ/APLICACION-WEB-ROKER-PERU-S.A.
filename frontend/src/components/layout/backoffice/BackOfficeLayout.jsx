import { Outlet } from "react-router-dom";
import BackOfficeHeader from "./BackOfficeHeader";
import BackOfficeSidebar from "./BackOfficeSideBar";
import PublicFooter from '../storefront/PublicFooter';
import { useBackOffice } from './BackOfficeProvider';
import { ROLES } from '@/lib/auth';

export default function BackOfficeLayout({ role = "manager" }) {
    const roleName = ROLES.find(item => item.id === role)?.label;
    const {loading, error, refresh} = useBackOffice();

    return (
        <div className="min-h-screen bg-gray-50">
            <BackOfficeHeader role={roleName} />
            <BackOfficeSidebar role={role} />
            <main className="min-h-[calc(100vh-4rem)] md:ml-64 md:pt-12">
                <div className="p-4 md:p-6 max-w-7xl mx-auto">
                    {loading ? <p className="p-8 text-gray-500">Cargando información del servidor...</p> : error ? <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-6"><p>{error}</p><button className="mt-3 text-brand underline" onClick={refresh}>Reintentar</button></div> : <Outlet />}
                </div>
            </main>
            <div className="md:ml-64"><PublicFooter /></div>
        </div>
    );
}
