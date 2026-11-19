import { Link, useNavigate } from 'react-router-dom';
import { LogOut } from 'lucide-react';
import AccountMenu from '@/components/shared/AccountMenu';
import NotificationMenu from './NotificationMenu';
import { useAuth } from '@/hooks/useAuth'; // Importamos el hook de autenticación
import logo from '@/assets/logos/logo.png';

export default function BackOfficeHeader() {
    const { logout } = useAuth(); // Extraemos la función de cerrar sesión
    const navigate = useNavigate();
    const exit = () => { logout(); navigate('/login', { replace: true }); };

    return (
        <header className="sticky top-0 z-50 border-b border-gray-200 bg-white">
            <div className="flex h-16 items-center justify-between px-4 md:px-6">
                <Link to="/" aria-label="Roker Perú - Inicio">
                    <img src={logo} alt="Roker Perú S.A." className="h-10 w-auto" />
                </Link>

                <div className="flex items-center gap-3">
                    <NotificationMenu />
                    <AccountMenu withLogout={false} />

                    {/* Botón rápido de cerrar sesión */}
                    <button
                        onClick={exit}
                        title="Cerrar sesión"
                        aria-label="Cerrar sesión"
                        className="flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 text-sm font-medium text-gray-700 transition hover:bg-red-50 hover:text-red-600 hover:border-red-200"
                    >
                        <LogOut size={18} />
                        <span className="hidden sm:inline">Salir</span>
                    </button>
                </div>
            </div>
        </header>
    );
}