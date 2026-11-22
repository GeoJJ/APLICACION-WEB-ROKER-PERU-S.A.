import { Link, Outlet } from "react-router-dom";
import logo from "@/assets/logos/logo.png";

export default function AuthLayout() {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4 py-12 sm:px-6 lg:px-8">
            <div className="mb-8 text-center">
                <Link to="/" aria-label="Roker Perú - Inicio" className="inline-block transition hover:opacity-90">
                    <img src={logo} alt="Roker Perú S.A." className="mx-auto h-16 w-auto" />
                </Link>
            </div>
            <Outlet />
        </div>
    );
}