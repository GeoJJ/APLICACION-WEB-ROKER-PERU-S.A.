import {createContext,useContext,useEffect,useRef,useState} from 'react';
import {useAuth} from '@/components/auth/AuthProvider';
import {emptyPanel,loadPanel} from '@/lib/backofficeApi';
const Context=createContext(null);
export default function BackOfficeProvider({children}){
 const {role}=useAuth();const [data,setData]=useState(emptyPanel);const [loaded,setLoaded]=useState('');const [notice,setNotice]=useState('');const [notes,setNotes]=useState([]);const [actorId,setActorId]=useState('');const generation=useRef(0);
 const roleId=role?.id||'',key=roleId+':'+actorId;
 useEffect(()=>{let active=true;const token=++generation.current;if(!roleId)return;
  loadPanel(roleId,actorId).then(result=>{if(active&&token===generation.current){setData(result.data);setNotes(result.notes);setLoaded(key);}});
  return()=>{active=false;};
 },[roleId,actorId,key]);
 async function refresh(){const token=++generation.current;const result=await loadPanel(roleId,actorId);if(token===generation.current){setData(result.data);setNotes(result.notes);setLoaded(key);}}
 async function update(){setNotice('La edición está pendiente de autenticación e integración con el backend actual. No se guardaron cambios.');return false;}
 return <Context.Provider value={{data:loaded===key?data:emptyPanel(),loading:Boolean(roleId&&loaded!==key),error:'',notice,setNotice,notes,refresh,update,actorId,setActorId,readOnly:true}}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useBackOffice=()=>useContext(Context);
