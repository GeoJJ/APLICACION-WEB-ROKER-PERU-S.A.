import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '@/lib/api';
const Context = createContext(null);
const key = 'roker-cart-v1';
function initialCart() {
 try {
  const saved=JSON.parse(localStorage.getItem(key));
  if(!Array.isArray(saved))return [];
  const unique=new Map();
  for(const row of saved.slice(0,100))if(Number.isSafeInteger(row.id)&&row.id>0&&Number.isSafeInteger(row.quantity)&&row.quantity>0&&row.quantity<=10000)unique.set(row.id,{id:row.id,quantity:row.quantity,name:String(row.name||'Producto'),price:Math.max(0,Number(row.price)||0),stock:0,available:false});
  return [...unique.values()];
 }catch{return [];}
}
function reconcile(items,products){return items.map(item=>{const p=products.find(p=>p.id===item.id);return p?{...p,quantity:item.quantity,available:p.available!==false}:{...item,stock:0,available:false};});}
export default function CartProvider({children}){
 const [items,setItems]=useState(initialCart);
 const [open,setOpen]=useState(false);
 const [toast,setToast]=useState(null);
 const [error,setError]=useState('');
 useEffect(()=>{try{localStorage.setItem(key,JSON.stringify(items.map(({id,name,quantity,price})=>({id,name,quantity,price}))));}catch{/* Cart remains usable when browser storage is unavailable. */}},[items]);
 useEffect(()=>{let active=true;api('/public/products').then(products=>{if(active)setItems(current=>reconcile(current,products));}).catch(e=>{if(active)setError(e.message);});return()=>{active=false;};},[]);
 useEffect(()=>{if(!toast)return;const timer=setTimeout(()=>setToast(null),9000);return()=>clearTimeout(timer);},[toast]);
 async function refresh(){try{const products=await api('/public/products');setItems(current=>reconcile(current,products));setError('');return products;}catch(e){setError(e.message);return null;}}
 function add(product){
  const quantity=items.find(p=>p.id===product.id)?.quantity||0;
  if(quantity>=(product.stock??10000)){setToast({name:product.name,error:'Alcanzaste el stock disponible.'});return;}
  setItems(current=>current.some(p=>p.id===product.id)?current.map(p=>p.id===product.id?{...product,available:true,quantity:p.quantity+1}:p):[...current,{...product,available:true,quantity:1}]);
  setToast({name:product.name});
 }
 function quantity(id,value){const item=items.find(p=>p.id===id);const n=Number(value);if(!item||!Number.isSafeInteger(n)||n<1)return;setItems(current=>current.map(p=>p.id===id?{...p,quantity:Math.min(n,Math.max(1,p.stock??10000))}:p));}
 function remove(id){setItems(current=>current.filter(p=>p.id!==id));}
 function openCart(){setToast(null);setOpen(true);refresh();}
 return <Context.Provider value={{items,add,quantity,remove,clear:()=>setItems([]),open,openCart,close:()=>setOpen(false),toast,dismissToast:()=>setToast(null),refresh,error,count:items.reduce((n,p)=>n+p.quantity,0),subtotal:items.reduce((n,p)=>n+p.quantity*p.price,0)}}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useCart=()=>useContext(Context);
