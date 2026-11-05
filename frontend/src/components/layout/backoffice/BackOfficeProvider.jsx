import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { useAuth } from '@/components/auth/AuthProvider';
import { api } from '@/lib/api';
const Context = createContext(null);
const empty = {workers:[],products:[],services:[],orders:[],deliveries:[],movements:[],providers:[],customers:[],team:[]};
export default function BackOfficeProvider({ children }) {
 const {user}=useAuth();
 const [data,setData]=useState(empty);const [loadedUser,setLoadedUser]=useState(null);const [loading,setLoading]=useState(false);const [notice,setNotice]=useState('');const [error,setError]=useState('');const busy=useRef(false);
 const refresh=useCallback(async () => { if(!user){setData(empty);return;}setLoading(true);setError('');try{setData(await api('/panel'));}catch(e){setError(e.message);}finally{setLoading(false);} },[user]);
 useEffect(() => {
  let active=true;
  if(user)api('/panel').then(value=>{if(active){setData(value);setError('');}}).catch(e=>{if(active)setError(e.message);}).finally(()=>{if(active)setLoadedUser(user.id);});
  return()=>{active=false;};
 },[user]);
 async function update(transform) {
  if(busy.current)return false;busy.current=true;
  try {
   const next=transform(data);let change;
   // A movement also changes stock, and an order can affect deliveries. Only send the source operation.
   for(const kind of ['movements','workers','providers','products','services','orders','deliveries']) {
    const record=next[kind]?.find(r => {const before=data[kind].find(x=>x.id===r.id);return !before||JSON.stringify(before)!==JSON.stringify(r);});
    if(record){change={kind,record,exists:data[kind].some(x=>x.id===record.id)};break;}
   }
   if(!change)return true;
   const {kind,record,exists}=change;
   setData(await api('/panel/'+kind+(exists?'/'+encodeURIComponent(record.id):''),{method:exists?'PUT':'POST',body:JSON.stringify(record)}));
   setNotice('Cambios guardados correctamente en el servidor.');return true;
  }catch(e){setNotice(e.message);return false;}finally{busy.current=false;}
 }
 return <Context.Provider value={{data:user?data:empty,update,refresh,loading:loading || Boolean(user && loadedUser !== user.id),error,notice,setNotice}}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useBackOffice=()=>useContext(Context);
