import { Link } from 'react-router-dom';
import AccountMenu from '@/components/auth/AccountMenu';
import NotificationMenu from '@/components/auth/NotificationMenu';
import logo from '@/assets/logos/logo.png';
export default function BackOfficeHeader() {
 return <header className="sticky top-0 z-50 border-b border-gray-200 bg-white"><div className="flex h-16 items-center justify-between px-4 md:px-6"><Link to="/" aria-label="Roker Perú - Inicio"><img src={logo} alt="Roker Perú S.A." className="h-10 w-auto" /></Link><div className="flex items-center gap-4"><NotificationMenu /><AccountMenu /></div></div></header>;
}
