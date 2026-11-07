import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import logo from '@/assets/logos/logo.png';
export function useCatalog(kind){
 const [items,setItems]=useState([]),[loading,setLoading]=useState(true),[error,setError]=useState('');
 useEffect(()=>{let active=true;api('/public/'+kind).then(rows=>{if(active)setItems(rows.map(r=>({...r,category:(r.category||'').toLowerCase(),image:logo})));}).catch(e=>{if(active)setError(e.message);}).finally(()=>{if(active)setLoading(false);});return()=>{active=false;};},[kind]);
 return {items,loading,error};
}
