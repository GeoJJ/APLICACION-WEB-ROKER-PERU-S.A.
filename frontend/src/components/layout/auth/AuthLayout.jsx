import { Link, Outlet, useLocation } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import logo from "@/assets/logos/logo.png";
import desinfeccion from "@/assets/auth/imagen-login.png";
import limpieza from "@/assets/auth/imagen-registro.png";

export default function AuthLayout() {
    const registro = useLocation().pathname.replace(/\/$/, "") === "/register";

    return (
        <main className="min-h-screen bg-white">
            <div className="grid min-h-screen w-full bg-white md:grid-cols-2">
                <section className={`relative min-h-56 overflow-hidden bg-blue-950 md:row-start-1 md:min-h-screen ${registro ? "order-1 md:col-start-1" : "order-2 md:col-start-2"}`}>
                    <img src={registro ? limpieza : desinfeccion} alt={registro ? "Servicio de limpieza de espacios" : "Desinfección profesional de espacios"} className="absolute inset-0 h-full w-full object-cover" />
                </section>
                <div className={`flex flex-col px-5 py-7 sm:px-10 md:row-start-1 lg:px-12 ${registro ? "order-2 md:col-start-2" : "order-1 md:col-start-1"}`}>
                    <div className="flex items-center justify-between gap-4">
                        <Link to="/" aria-label="Roker Perú - Inicio"><img src={logo} alt="Roker Perú S.A." className="h-12 w-auto" /></Link>
                        <Link to="/" className="inline-flex items-center gap-2 text-sm font-medium text-blue-900 hover:text-blue-700"><ArrowLeft size={16} /> Inicio</Link>
                    </div>
                    <div className="flex flex-1 items-center justify-center py-6"><Outlet /></div>
                </div>
            </div>
        </main>
    );
}
