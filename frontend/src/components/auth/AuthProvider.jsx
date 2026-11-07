import {createContext,useContext} from 'react';
import {useLocation} from 'react-router-dom';
import {ROLES} from '@/data/roles';
import {pending} from '@/lib/api';
const Context=createContext(null);
export default function AuthProvider({children}){
 const {pathname}=useLocation();
 const role=ROLES.find(r=>r.id!=='customer'&&(pathname===r.home||pathname.startsWith(r.home+'/')));
 async function login(){throw pending('Inicio de sesión');}
 async function profile(){throw pending('Edición del perfil');}
 async function logout(){throw pending('Cierre de sesión');}
 return <Context.Provider value={{user:null,role,loading:false,error:'',login,profile,logout,preview:true}}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useAuth=()=>useContext(Context);
