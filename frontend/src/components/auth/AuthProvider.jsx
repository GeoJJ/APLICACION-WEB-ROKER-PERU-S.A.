import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { ROLES } from '@/data/roles';
const Context = createContext(null);
export default function AuthProvider({ children }) {
 const [user, setUser] = useState(null);
 const [loading, setLoading] = useState(true);
 const [error, setError] = useState('');
 useEffect(() => {
  let active = true;
  api('/auth/me').then(u => { if(active)setUser(u); }).catch(e => { if(active && e.status !== 401)setError(e.message); }).finally(() => { if(active)setLoading(false); });
  const expired = () => setUser(null);
  window.addEventListener('roker-session-expired', expired);
  return () => { active=false; window.removeEventListener('roker-session-expired', expired); };
 }, []);
 async function login(email,password) { const u=await api('/auth/login',{method:'POST',body:JSON.stringify({email,password})});setUser(u);setError('');return u; }
 async function logout() { await api('/auth/logout',{method:'POST'});setUser(null); }
 async function profile(values) { const u=await api('/auth/profile',{method:'PUT',body:JSON.stringify(values)});setUser(u);return u; }
 const role=ROLES.find(r => r.code === user?.role);
 return <Context.Provider value={{user,role,loading,error,login,logout,profile}}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(Context);
