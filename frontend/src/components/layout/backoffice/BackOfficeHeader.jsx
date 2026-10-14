import { Bell, ChevronDown, LogOut } from "lucide-react";
import { useState } from "react";
import logo from "@/assets/logos/logo.png";

export default function BackOfficeHeader({ role = "Administrador" }) {
    const [dropdownOpen, setDropdownOpen] = useState(false);

    const roleColors = {
        Administrador: "bg-blue-600",
        Almacenista: "bg-green-600",
        Vendedor: "bg-purple-600",
        Repartidor: "bg-orange-600",
    };

    return (
        <header className="sticky top-0 z-50 border-b border-gray-200 bg-white">
            <div className="flex h-16 items-center justify-between px-6">
                <img src={logo} alt="Rocket Peru S.A." className="h-10 w-auto" />

                <div className="flex items-center gap-4">
                    <button className="relative rounded-full p-2 text-gray-600 transition hover:bg-gray-100">
                        <Bell size={20} />
                        <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-red-500" />
                    </button>

                    <div className="relative">
                        <button
                            onClick={() => setDropdownOpen(!dropdownOpen)}
                            className="flex items-center gap-2 rounded-lg px-3 py-2 transition hover:bg-gray-100"
                        >
                            <div className={`flex h-8 w-8 items-center justify-center rounded-full ${roleColors[role]}`}>
                                <span className="text-sm font-semibold text-white">{role.charAt(0)}</span>
                            </div>
                            <span className="text-sm font-semibold text-gray-700">{role}</span>
                            <ChevronDown size={16} className="text-gray-500" />
                        </button>

                        {dropdownOpen && (
                            <div className="absolute right-0 mt-2 w-48 rounded-xl border border-gray-200 bg-white py-2 shadow-lg">
                                <a href="/profile" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Mi Perfil</a>
                                <hr className="my-2 border-gray-200" />
                                <a href="/" className="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100 flex items-center gap-2">
                                    <LogOut size={16} /> Cerrar Sesión
                                </a>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </header>
    );
}