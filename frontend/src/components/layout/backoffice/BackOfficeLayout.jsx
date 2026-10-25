import { Outlet } from "react-router-dom";
import BackOfficeHeader from "./BackOfficeHeader";
import BackOfficeSidebar from "./BackOfficeSidebar";

export default function BackOfficeLayout({ role = "admin" }) {
    const roleNames = { admin: "Administrador", warehouse: "Almacenista", seller: "Vendedor", delivery: "Repartidor" };

    return (
        <div className="min-h-screen bg-gray-50">
            <BackOfficeHeader role={roleNames[role]} />
            <BackOfficeSidebar role={role} />
            <main className="ml-64 pt-16">
                <div className="p-6 max-w-7xl mx-auto">
                    <Outlet />
                </div>
            </main>
        </div>
    );
}