export const INTEGRATION = { authentication: false, checkout: false, quotations: false };
export const pending = feature => new Error(feature + ' pendiente de integración con el backend de esta rama.');
const base=(import.meta.env.VITE_API_URL||'/api').replace(/\/$/,'');
export async function request(path,options={}){
 const response=await fetch(base+path,{...options,credentials:'include',headers:{...(options.body?{'Content-Type':'application/json'}:{}),...options.headers}});
 if(!response.ok){const data=await response.json().catch(()=>({}));const error=new Error(data.message||data.mensaje||('No se pudo consultar el servidor ('+response.status+').'));error.status=response.status;throw error;}
 if(options.blob)return response.blob();
 const text=await response.text();return text?JSON.parse(text):null;
}
export function publicProduct(p){return{id:p.idProducto,name:p.nombreProducto,category:p.categoriaProducto,price:p.precioProducto,stock:p.stockProducto??(p.disponible?null:0),available:p.disponible!==false};}
export function publicService(s){return{id:s.idServicio??s.nombreServicio,name:s.nombreServicio,category:s.categoriaServicio,description:s.descripcionServicio};}
export async function api(path,options={}){
 if(path.startsWith('/auth/'))throw pending('Autenticación y perfil');
 if(path.startsWith('/solicitudes'))throw pending('Solicitudes y cotizaciones');
 if(path.startsWith('/checkout'))throw pending('Confirmación de compras');
 if(path.startsWith('/panel'))throw pending('Gestión');
 try{
  if(path==='/public/products')return(await request('/cliente/productos',options)).map(publicProduct);
  if(path==='/public/services')return(await request('/cliente/servicios',options)).map(publicService);
  if(path==='/reportes/productos/excel')return request('/gerente/reportes/productos/excel',options);
  return await request(path,options);
 }catch(error){if(error instanceof TypeError)throw new Error('No se pudo conectar con el backend. Comprueba que esté ejecutándose.',{cause:error});throw error;}
}
