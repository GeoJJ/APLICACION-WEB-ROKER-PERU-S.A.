import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronDown, CircleUser, LayoutDashboard } from 'lucide-react';
import { useAuth } from './AuthProvider';
import { ROLES } from '@/data/roles'; // <-- Importamos tu lista de roles

export default function AccountMenu() {
    const { role } = useAuth(); // role aquí es el texto "admin"
    const [open, setOpen] = useState(false);

    // Si no hay rol, muestra el botón de Iniciar Sesión
    if (!role) return (
        <Link to="/login" aria-label="Iniciar sesión" className="text-brand">
            <CircleUser size={30} />
        </Link>
    );

    // Buscamos el objeto completo para obtener el color y la etiqueta (ej. "Administrador")
    const roleData = ROLES.find(r => r.id === role) || ROLES.find(r => r.id === 'customer');

    return (
        <div className="relative">
            <button 
                aria-label="Menú del panel" 
                aria-expanded={open} 
                onClick={() => setOpen(!open)} 
                className="flex items-center gap-2 rounded-lg px-2 py-2 hover:bg-gray-50"
            >
                {/* Ahora usamos roleData para sacar el color y la inicial */}
                <span className={`grid h-8 w-8 place-items-center rounded-full text-sm font-semibold text-white ${roleData.color}`}>
                    {roleData.label[0]}
                </span>
                <span className="hidden text-sm font-semibold text-gray-700 sm:inline">
                    {roleData.label}
                </span>
                <ChevronDown size={16} />
            </button>
            
            {open && (
                <div className="absolute right-0 z-50 mt-2 w-64 rounded-xl border border-gray-200 bg-white p-4 shadow-lg">
                    <Link to={roleData.home} onClick={() => setOpen(false)} className="flex items-center gap-2 text-sm text-brand">
                        <LayoutDashboard size={17} />
                        Panel de gestión
                    </Link>
                </div>
            )}
        </div>
    );
}