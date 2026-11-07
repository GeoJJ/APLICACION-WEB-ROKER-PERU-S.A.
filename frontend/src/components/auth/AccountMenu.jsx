import {useState} from 'react';
import {Link} from 'react-router-dom';
import {ChevronDown,CircleUser,LayoutDashboard} from 'lucide-react';
import {useAuth} from './AuthProvider';
export default function AccountMenu(){
 const {role}=useAuth();const [open,setOpen]=useState(false);
 if(!role)return <Link to="/login" aria-label="Iniciar sesión" className="text-brand"><CircleUser size={30}/></Link>;
 return <div className="relative"><button aria-label="Menú del panel" aria-expanded={open} onClick={()=>setOpen(!open)} className="flex items-center gap-2 rounded-lg px-2 py-2 hover:bg-gray-50"><span className={`grid h-8 w-8 place-items-center rounded-full text-sm font-semibold text-white ${role.color}`}>{role.label[0]}</span><span className="hidden text-sm font-semibold text-gray-700 sm:inline">{role.label}</span><ChevronDown size={16}/></button>{open&&<div className="absolute right-0 z-50 mt-2 w-64 rounded-xl border border-gray-200 bg-white p-4 shadow-lg"><p className="mb-3 text-xs text-gray-500">Vista previa del panel. No hay una sesión iniciada.</p><Link to={role.home} onClick={()=>setOpen(false)} className="flex items-center gap-2 text-sm text-brand"><LayoutDashboard size={17}/>Panel de gestión</Link></div>}</div>;
}
