import {request} from './api';
export const emptyPanel=()=>({workers:[],products:[],services:[],orders:[],deliveries:[],movements:[],providers:[],customers:[],team:[]});
const state=s=>['ENTREGADO','COMPLETADO'].includes(s)?'Completado':['EN_RUTA','CONFIRMADO','EN_PROCESO'].includes(s)?'En camino':s==='CANCELADO'?'Cancelado':'Pendiente';
const product=p=>({id:p.idProducto,codigo:p.codigoProducto,producto:p.nombreProducto,categoria:p.categoriaProducto,precio:p.precioProducto,stock:p.stockProducto,umbral:p.stockMinimo,estado:p.estadoProducto==='INACTIVO'?'Inactivo':'Activo',idProveedor:p.idProveedor});
const service=s=>({id:s.idServicio,nombre:s.nombreServicio,descripcion:s.descripcionServicio,precio:s.precioServicio,categoria:s.categoriaServicio,duracion:s.duracionEstimadaServicio,personal:s.cantidadPersonalServicio,estado:'Sin información'});
const provider=p=>({id:p.idProveedor,nombre:p.nombreProveedor,ruc:p.rucProveedor,telefono:p.telefonoProveedor,email:p.correoProveedor});
const movement=m=>({id:m.idMovimiento,producto:m.nombreProducto,fecha:m.fechaOperacion?.slice(0,10),cantidad:m.cantidadMovida,tipo:m.tipoMovimiento,proveedor:'No informado por la API',observaciones:m.mensaje});
const order=o=>({id:'P-'+o.idPedido,cliente:o.nombreCliente,tipo:'Producto',total:o.montoTotalPedido,estado:state(o.estadoPedido),fecha:o.fechaPedido?.slice(0,10),direccion:o.direccionCliente,telefono:'No informado por la API',concepto:o.detalles?.map(d=>d.nombreProducto+' × '+d.cantidadProducto).join(', '),cantidad:o.detalles?.reduce((n,d)=>n+d.cantidadProducto,0),idDelivery:o.idDelivery});
const serviceOrder=o=>({id:'S-'+o.idOrdenServicio,cliente:o.nombreCliente,tipo:'Servicio',total:o.montoTotalServicio,estado:state(o.estadoServicio),fecha:o.fechaOrdenServicio?.slice(0,10),direccion:'No informada por la API',telefono:'No informado por la API',concepto:o.nombreServicio,cantidad:1,observaciones:o.incidenciaServicio});
const delivery=d=>({id:'P-'+d.idPedido,cliente:d.nombreCliente,direccion:d.direccionEntregaCliente,telefono:d.telefonoCliente||'',estado:d.estadoPedido==='ENTREGADO'?'Entregado':state(d.estadoPedido),fecha:d.fechaEntregaPedido?.slice(0,10)||'',fechaEntrega:d.fechaEntregaPedido?.slice(0,10)||'',observaciones:d.incidentePedido||'',evidencia:'',firma:''});
export async function loadPanel(role,actorId){
 const data=emptyPanel(),notes=[];const jobs=[];
 function add(path,key,map){jobs.push({path,key,map});}
 if(role==='manager'){add('/gerente/inventario/movimientos','movements',movement);notes.push('GERENTE_GENERAL dispone de reportes e inventario. El backend reserva la gestión de usuarios, productos y servicios a ADMINISTRADOR.');}
 if(role==='warehouse'){add('/almacenista/productos','products',product);add('/almacenista/proveedores','providers',provider);add('/almacenista/inventario/movimientos','movements',movement);}
 if(role==='seller'){add('/vendedor/productos','products',product);add('/vendedor/servicios','services',service);add('/vendedor/pedidos','orders',order);add('/vendedor/ordenes-servicio','orders',serviceOrder);add('/vendedor/repartidores','team',u=>({id:u.idUsuario,nombre:u.nombreUsuario,rol:'delivery'}));notes.push('La API de pedidos devuelve únicamente pendientes. No ofrece un historial completo de ventas.');}
 if(role==='delivery'){if(/^[1-9][0-9]*$/.test(actorId))add('/repartidor/pedidos?idRepartidor='+encodeURIComponent(actorId),'deliveries',delivery);else notes.push('Indica el ID de repartidor para consultar sus pedidos. El ID es un filtro de consulta y no inicia sesión.');notes.push('La API no devuelve fecha del pedido, fotografía ni firma, y solo lista entregas activas.');}
 if(role==='staff')notes.push('El backend actual no dispone de una consulta de órdenes para personal de servicio.');
 const results=await Promise.allSettled(jobs.map(async job=>({job,rows:await request(job.path)})));
 results.forEach(result=>{if(result.status==='fulfilled'){const {job,rows}=result.value;data[job.key].push(...rows.map(job.map));}else notes.push('No se pudo cargar información del servidor: '+result.reason.message);});
 return{data,notes};
}
