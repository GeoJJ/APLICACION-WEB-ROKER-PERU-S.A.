import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ChevronDown, CircleUser, ClipboardList, LayoutDashboard, LogOut, UserRound } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { roleById } from '@/lib/auth';

const itemClass = 'flex w-full items-center gap-2 rounded-lg px-2 py-2 text-sm text-gray-700 hover:bg-gray-50';

export default function AccountMenu({ withLogout = true }) {
    const { role, user, logout } = useAuth();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);

    if (!role) return (
        <Link to="/login" aria-label="Iniciar sesión" className="text-brand">
            <CircleUser size={30} />
        </Link>
    );

    const info = roleById(role);
    const isCustomer = role === 'customer';
    const close = () => setOpen(false);
    const exit = () => { close(); logout(); navigate(isCustomer ? '/' : '/login', { replace: true }); };

    return (
        <div className="relative">
            <button aria-label="Menú de la cuenta" aria-expanded={open} onClick={() => setOpen(!open)}
                className="flex items-center gap-2 rounded-lg px-2 py-2 hover:bg-gray-50">
                <span className={`grid h-8 w-8 place-items-center rounded-full text-sm font-semibold text-white ${info.color}`}>
                    {info.label[0]}
                </span>
                <span className="hidden text-sm font-semibold text-gray-700 sm:inline">{info.label}</span>
                <ChevronDown size={16} />
            </button>

            {open && (
                <div className="absolute right-0 z-50 mt-2 w-64 rounded-xl border border-gray-200 bg-white p-3 shadow-lg">
                    <p className="truncate px-2 pb-2 text-xs text-gray-500">{user?.email}</p>
                    {isCustomer ? (
                        <>
                            <Link to="/perfil" onClick={close} className={itemClass}><UserRound size={17} />Mi perfil</Link>
                            <Link to="/mis-solicitudes" onClick={close} className={itemClass}><ClipboardList size={17} />Mis solicitudes</Link>
                        </>
                    ) : (
                        <Link to={info.home} onClick={close} className={itemClass}><LayoutDashboard size={17} />Panel de gestión</Link>
                    )}
                    {withLogout && <button onClick={exit} className={itemClass}><LogOut size={17} />Cerrar sesión</button>}
                </div>
            )}
        </div>
    );
}