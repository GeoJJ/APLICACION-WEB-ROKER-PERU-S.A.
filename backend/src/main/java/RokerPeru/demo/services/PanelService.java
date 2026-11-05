package RokerPeru.demo.services;

import RokerPeru.demo.dto.*;
import RokerPeru.demo.entities.*;
import RokerPeru.demo.repository.*;
import RokerPeru.demo.security.CurrentUser;
import jakarta.validation.Validator;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor @Transactional
public class PanelService {
 private final CurrentUser current;
 private final UsuarioRepository users; private final RolRepository roles;
 private final ProductoRepository products; private final ServicioRepository services;
 private final ProveedoresRepository providers; private final PedidosRepository orders;
 private final OrdenesServicioRepository serviceOrders; private final EmpleadosServicioRepository employees;
 private final MovimientosProductoRepository movements; private final AsignacionesRepository assignments;
 private final UsuarioService userService; private final ProductoService productService;
 private final ServicioService serviceService; private final PedidoService orderService;
 private final OrdenServicioService serviceOrderService; private final InventarioService inventory;
 private final PasswordEncoder encoder; private final Validator validator;
 private static final Map<String,String> ROLE_CODES=Map.of("admin","ADMINISTRADOR","warehouse","ALMACENISTA","seller","VENDEDOR","delivery","REPARTIDOR","staff","PERSONAL","customer","CLIENTE");
 private static Map<String,Object> row(Object... entries) { var map=new LinkedHashMap<String,Object>(); for(int i=0;i<entries.length;i+=2) map.put((String)entries[i],entries[i+1]==null?"":entries[i+1]); return map; }
 private static String text(Map<String,Object> m,String key) { return Objects.toString(m.get(key),"").trim(); }
 private static long number(Map<String,Object> m,String key) { try{return Long.parseLong(text(m,key));}catch(Exception e){throw new IllegalArgumentException("Completa el campo: "+key);} }
 private static double decimal(Map<String,Object> m,String key) { try{double n=Double.parseDouble(text(m,key)); if(!Double.isFinite(n)||n<0)throw new Exception(); return n;}catch(Exception e){throw new IllegalArgumentException("Importe no válido: "+key);} }
 private <T> T valid(T dto) { var errors=validator.validate(dto); if(!errors.isEmpty()) throw new IllegalArgumentException(errors.iterator().next().getMessage()); return dto; }
 private static String roleCode(String role) { return ROLE_CODES.entrySet().stream().filter(e -> e.getValue().equalsIgnoreCase(role)).map(Map.Entry::getKey).findFirst().orElse("customer"); }
 private static String status(String s) { return switch(Objects.toString(s,"").toUpperCase(Locale.ROOT)) {case "ENTREGADO","COMPLETADO" -> "Completado"; case "EN_RUTA","EN_PROCESO","CONFIRMADO" -> "En camino"; case "CANCELADO" -> "Cancelado"; default -> "Pendiente";}; }
 private Map<String,Object> worker(Usuario u) { return row("id",u.getIdUsuario(),"nombre",u.getNombreUsuario(),"email",u.getCorreoUsuario(),"telefono",u.getTelefonoUsuario(),"direccion",u.getDireccionUsuario(),"documento",u.getNumeroDocumentoUsuario(),"tipoPersona",u.getTipoPersonaUsuario(),"rol",roleCode(u.getRol().getNombreRol()),"estado",u.isActivo()?"Activo":"Inactivo"); }
 private Map<String,Object> product(Producto p) { return row("id",p.getIdProducto(),"producto",p.getNombreProducto(),"codigo",p.getCodigoProducto(),"categoria",p.getCategoriaProducto(),"precio",p.getPrecioProducto(),"stock",p.getStockProducto(),"umbral",p.getStockMinimo(),"estado","INACTIVO".equalsIgnoreCase(p.getEstadoProducto())?"Inactivo":"Activo","idProveedor",p.getProveedor()==null?null:p.getProveedor().getIdProveedor()); }
 private Map<String,Object> service(Servicio s) { return row("id",s.getIdServicio(),"nombre",s.getNombreServicio(),"descripcion",s.getDescripcionServicio(),"precio",s.getPrecioServicio(),"categoria",s.getCategoriaServicio(),"duracion",s.getDuracionEstimadaServicio(),"personal",s.getCantidadPersonalServicio(),"estado",s.isActivo()?"Activo":"Inactivo"); }
 private Map<String,Object> order(Pedidos p) { return row("id","P-"+p.getIdPedido(),"cliente",p.getCliente().getNombreUsuario(),"clienteId",p.getCliente().getIdUsuario(),"tipo","Producto","fecha",p.getFechaPedido().toLocalDate().toString(),"total",p.getMontoTotalPedido(),"estado",status(p.getEstadoPedido()),"direccion",p.getDireccionEntrega()!=null?p.getDireccionEntrega():p.getCliente().getDireccionUsuario(),"telefono",p.getTelefonoEntrega()!=null?p.getTelefonoEntrega():p.getCliente().getTelefonoUsuario(),"concepto",p.getDetallePedidos().stream().map(d -> d.getProducto().getNombreProducto()+" × "+d.getCantidadProducto()).reduce((a,b)->a+", "+b).orElse(""),"cantidad",p.getDetallePedidos().stream().mapToInt(DetallePedido::getCantidadProducto).sum(),"idDelivery",p.getDelivery()==null?null:p.getDelivery().getIdUsuario(),"comprobante",p.getNumeroComprobantePedido()); }
 private Map<String,Object> serviceOrder(OrdenesServicio o) { return row("id","S-"+o.getIdOrdenServicio(),"cliente",o.getCliente().getNombreUsuario(),"clienteId",o.getCliente().getIdUsuario(),"tipo","Servicio","fecha",o.getFechaOrdenServicio().toLocalDate().toString(),"total",o.getMontoTotalServicio(),"estado",status(o.getEstadoServicio()),"direccion",o.getCliente().getDireccionUsuario(),"telefono",o.getCliente().getTelefonoUsuario(),"concepto",o.getServicio().getNombreServicio(),"cantidad",1,"personalIds",o.getAsignaciones().stream().filter(a -> a.getEmpleado().getUsuario()!=null).map(a -> a.getEmpleado().getUsuario().getIdUsuario()).toList(),"personalId",o.getAsignaciones().isEmpty()?null:o.getAsignaciones().get(0).getEmpleado().getUsuario()==null?null:o.getAsignaciones().get(0).getEmpleado().getUsuario().getIdUsuario(),"observaciones",o.getIncidenciaServicio(),"detalleSolicitud",o.getDetalleSolicitud(),"comprobante",o.getNumeroComprobanteServicio()); }
 private Map<String,Object> delivery(Pedidos p) { var r=order(p); r.put("estado","ENTREGADO".equals(p.getEstadoPedido())?"Entregado":status(p.getEstadoPedido())); r.put("observaciones",Objects.toString(p.getIncidentePedido(),"")); r.put("evidencia",Objects.toString(p.getEvidencia(),""));r.put("firma",Objects.toString(p.getFirma(),""));r.put("fechaEntrega",p.getFechaEntregaPedido()==null?"":p.getFechaEntregaPedido().toLocalDate().toString());return r; }
 private boolean assigned(OrdenesServicio o,Long user) { return o.getAsignaciones().stream().anyMatch(a -> a.getEmpleado().getUsuario()!=null && a.getEmpleado().getUsuario().getIdUsuario().equals(user)); }
 @Transactional(readOnly=true)
 public Map<String,Object> snapshot() {
  var u=current.get();String role=current.role();var result=new LinkedHashMap<String,Object>();
  boolean admin="ADMINISTRADOR".equals(role), sales="VENDEDOR".equals(role), stock="ALMACENISTA".equals(role);
  result.put("workers",admin?users.findAllByOrderByNombreUsuarioAsc().stream().filter(w -> !"CLIENTE".equalsIgnoreCase(w.getRol().getNombreRol())).map(this::worker).toList():List.of());
  result.put("customers",(admin||sales)?users.findByRol_NombreRolIgnoreCase("CLIENTE").stream().map(this::worker).toList():List.of());
  result.put("team",(admin||sales)?users.findAllByOrderByNombreUsuarioAsc().stream().filter(Usuario::isActivo).filter(w -> List.of("PERSONAL","REPARTIDOR").contains(w.getRol().getNombreRol().toUpperCase(Locale.ROOT))).map(w -> row("id",w.getIdUsuario(),"nombre",w.getNombreUsuario(),"rol",roleCode(w.getRol().getNombreRol()))).toList():List.of());
  result.put("products",(admin||sales||stock)?products.findAll().stream().map(this::product).toList():List.of());
  result.put("services",(admin||sales)?services.findAll().stream().map(this::service).toList():List.of());
  result.put("providers",(admin||stock)?providers.findAll().stream().map(p -> row("id",p.getIdProveedor(),"nombre",p.getNombreProveedor(),"ruc",p.getRucProveedor(),"telefono",p.getTelefonoProveedor(),"email",p.getCorreoProveedor())).toList():List.of());
  var list=new ArrayList<Map<String,Object>>();
  if(admin||sales) orders.findAll().forEach(p -> list.add(order(p)));
  if(admin||sales||"PERSONAL".equals(role)) serviceOrders.findAll().stream().filter(o -> !"PERSONAL".equals(role)||assigned(o,u.getIdUsuario())).forEach(o -> list.add(serviceOrder(o)));
  result.put("orders",list);
  result.put("deliveries","REPARTIDOR".equals(role)?orders.findAll().stream().filter(p -> p.getDelivery()!=null&&p.getDelivery().getIdUsuario().equals(u.getIdUsuario())).map(this::delivery).toList():List.of());
  result.put("movements",(admin||stock)?movements.findAllByOrderByFechaMovimientoProductoDesc().stream().map(m -> row("id",m.getIdMovimientoProducto(),"producto",m.getProducto().getNombreProducto(),"fecha",m.getFechaMovimientoProducto().toLocalDate().toString(),"cantidad",m.getCantidadMovimientoProducto(),"tipo",m.getTipoMovimientoProducto(),"proveedor",m.getProveedor(),"observaciones",m.getObservaciones())).toList():List.of());
  return result;
 }
 public void save(String kind,String id,Map<String,Object> m) {
  switch(kind) {
   case "workers" -> saveWorker(id,m);
   case "products" -> saveProduct(id,m);
   case "services" -> saveService(id,m);
   case "providers" -> saveProvider(id,m);
   case "movements" -> saveMovement(m);
   case "orders" -> saveOrder(id,m);
   case "deliveries" -> saveDelivery(id,m);
   default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  }
 }
 private void saveWorker(String id,Map<String,Object> m) {
  current.require("ADMINISTRADOR"); String name=ROLE_CODES.get(text(m,"rol"));
  if(name==null||"CLIENTE".equals(name))throw new IllegalArgumentException("Selecciona un rol de trabajador");
  var role=roles.findByNombreRolIgnoreCase(name).orElseThrow();
  if("ADMINISTRADOR".equals(name)&&users.findByRol_NombreRolIgnoreCase(name).stream().anyMatch(u->id==null||!u.getIdUsuario().equals(Long.valueOf(id))))throw new IllegalArgumentException("Solo puede existir una cuenta de administrador");
  Usuario previous=id==null?null:users.findById(Long.valueOf(id)).orElseThrow();
  if(previous!=null && previous.getIdUsuario().equals(current.get().getIdUsuario())&&(!"ADMINISTRADOR".equals(name)||!"Activo".equals(text(m,"estado"))))throw new IllegalArgumentException("No puedes quitar tu propio acceso de administrador");
  String password=text(m,"password"); if(previous==null&&password.length()<8)throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
  var dto=valid(new UsuarioDTO.Request(text(m,"nombre"),text(m,"email"),text(m,"tipoPersona").isBlank()?"NATURAL":text(m,"tipoPersona"),text(m,"documento"),text(m,"telefono"),text(m,"direccion"),role.getIdRol(),password.isBlank()?null:password));
  var response=previous==null?userService.crearUsuario(dto):userService.actualizarUsuario(previous.getIdUsuario(),dto);
  var user=users.findById(response.idUsuario()).orElseThrow();user.setActivo("Activo".equals(text(m,"estado")));
  if("PERSONAL".equals(name)&&employees.findAll().stream().noneMatch(e -> e.getUsuario()!=null&&e.getUsuario().getIdUsuario().equals(user.getIdUsuario()))) {var e=new EmpleadosServicio(); e.setNombreEmpleadoServicio(user.getNombreUsuario());e.setApellidoEmpleadoServicio("Equipo");e.setUsuario(user);employees.save(e);}
 }
 private void saveProduct(String id,Map<String,Object> m) {
  current.require("ADMINISTRADOR");
  var dto=valid(new ProductoDTO.Request(text(m,"codigo"),text(m,"producto"),text(m,"categoria"),decimal(m,"precio"),0,Math.toIntExact(number(m,"umbral")),"Activo".equals(text(m,"estado"))?"ACTIVO":"INACTIVO",number(m,"idProveedor")));
  if(id==null) productService.crearProducto(dto);else productService.actualizarProducto(Long.valueOf(id),dto);
 }
 private void saveService(String id,Map<String,Object> m) {
  current.require("ADMINISTRADOR");
  if(number(m,"personal")<1)throw new IllegalArgumentException("Indica al menos una persona para el servicio");
  var dto=valid(new ServicioDTO.Request(text(m,"categoria"),text(m,"nombre"),"GENERAL",text(m,"descripcion"),decimal(m,"precio"),text(m,"duracion"),text(m,"personal")));
  var response=id==null?serviceService.crearServicio(dto):serviceService.actualizarServicio(Long.valueOf(id),dto);
  services.findById(response.idServicio()).orElseThrow().setActivo("Activo".equals(text(m,"estado")));
 }
 private void saveProvider(String id,Map<String,Object> m) {
  current.require("ADMINISTRADOR");
  if(text(m,"nombre").isBlank()||!text(m,"ruc").matches("\\d{11}"))throw new IllegalArgumentException("Indica el nombre y un RUC de 11 dígitos");
  var p=id==null?new Proveedores():providers.findById(Long.valueOf(id)).orElseThrow();
  p.setNombreProveedor(text(m,"nombre"));p.setRucProveedor(text(m,"ruc"));p.setTelefonoProveedor(text(m,"telefono"));p.setCorreoProveedor(text(m,"email")); providers.save(p);
 }
 private void saveMovement(Map<String,Object> m) {
  current.require("ALMACENISTA","ADMINISTRADOR");
  var dto=valid(new MovimientoDTO.Request(number(m,"productId"),Math.toIntExact(number(m,"cantidad")),"INGRESO",text(m,"observaciones")));
  var r=inventory.registrarIngreso(dto);var movement=movements.findById(r.idMovimiento()).orElseThrow();movement.setObservaciones(text(m,"observaciones"));movement.setProveedor(text(m,"proveedor"));
  var date=LocalDate.parse(text(m,"fecha"));if(date.isAfter(LocalDate.now()))throw new IllegalArgumentException("La fecha no puede ser futura"); movement.setFechaMovimientoProducto(date.atTime(LocalTime.now()));
 }
 private void saveOrder(String id,Map<String,Object> m) {
  current.require("VENDEDOR","PERSONAL");
  if(id==null) {
   current.require("VENDEDOR");Long customerId=number(m,"clienteId");var c=users.findById(customerId).orElseThrow();
   if(!c.isActivo()||!"CLIENTE".equalsIgnoreCase(c.getRol().getNombreRol()))throw new IllegalArgumentException("Selecciona un cliente activo");
   if("Producto".equals(text(m,"tipo"))) {var dto=valid(new PedidoDTO.RequestCliente("EFECTIVO",customerId,List.of(new DetallePedidoDTO.Request(number(m,"item"),Math.toIntExact(number(m,"cantidad"))))));orderService.crearPedido(dto);}
   else {var s=services.findById(number(m,"item")).orElseThrow();if(!s.isActivo())throw new IllegalArgumentException("Servicio inactivo");serviceOrderService.crearOrdenServicio(valid(new OrdenServicioDTO.RequestCliente(s.getIdServicio(),customerId,LocalDateTime.now(),"EFECTIVO","")));}
   return;
  }
  if(id.startsWith("P-")) {
   current.require("VENDEDOR");Long key=Long.valueOf(id.substring(2));var p=orders.findById(key).orElseThrow();String next=text(m,"estado");
   if("En camino".equals(next)&&"PENDIENTE".equals(p.getEstadoPedido()))orderService.confirmarYAsignarPedido(key,new PedidoDTO.RequestAsignacionVendedor(number(m,"idDelivery"),LocalDateTime.now(),null),current.get().getIdUsuario());
   else if("Cancelado".equals(next))orderService.cancelarCliente(key,p.getCliente().getIdUsuario());
   else if(!status(p.getEstadoPedido()).equals(next))throw new IllegalArgumentException("La entrega debe ser confirmada por el repartidor asignado");
  } else {
   var o=serviceOrders.findById(Long.valueOf(id.substring(2))).orElseThrow();String next=text(m,"estado");
   if("PERSONAL".equals(current.role())) {
    if(!assigned(o,current.get().getIdUsuario()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if(!List.of("CONFIRMADO","EN_PROCESO").contains(o.getEstadoServicio())||!List.of("En camino","Completado").contains(next))throw new IllegalArgumentException("Transición de servicio no válida");
    o.setEstadoServicio("Completado".equals(next)?"COMPLETADO":"EN_PROCESO");o.setIncidenciaServicio(text(m,"observaciones"));return;
   }
   current.require("VENDEDOR");
   if("En camino".equals(next)&&"PENDIENTE".equals(o.getEstadoServicio())) {
    var selected=new java.util.HashSet<Long>();
    if(m.get("personalIds") instanceof java.util.List<?> ids) for(var userId:ids)selected.add(Long.valueOf(userId.toString()));
    else selected.add(number(m,"personalId"));
    if(selected.isEmpty())throw new IllegalArgumentException("Selecciona personal de servicio");
    for(Long userId:selected) {
     var employee=employees.findAll().stream().filter(e -> e.getUsuario()!=null&&e.getUsuario().isActivo()&&"PERSONAL".equalsIgnoreCase(e.getUsuario().getRol().getNombreRol())&&e.getUsuario().getIdUsuario().equals(userId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Selecciona personal de servicio activo"));
     if(o.getAsignaciones().stream().noneMatch(a -> a.getEmpleado().getIdEmpleadoServicio().equals(employee.getIdEmpleadoServicio()))) {var a=new Asignaciones();a.setEmpleado(employee);a.setOrdenServicio(o);a.setFechaAsignacion(LocalDateTime.now());a.setNombreResponsableAsignacion(current.get().getNombreUsuario());assignments.save(a);o.getAsignaciones().add(a);}
    }
    serviceOrderService.confirmarOrdenServicio(o.getIdOrdenServicio(),new OrdenServicioDTO.RequestGestionVendedor("CONFIRMADO",text(m,"observaciones")));o.setVendedor(current.get());
   } else if("Cancelado".equals(next))serviceOrderService.cancelarOrdenServicio(o.getIdOrdenServicio(),new OrdenServicioDTO.RequestGestionVendedor("CANCELADO",text(m,"observaciones")));
   else if(!status(o.getEstadoServicio()).equals(next))throw new IllegalArgumentException("El personal asignado debe completar el servicio");
  }
 }
 private void saveDelivery(String id,Map<String,Object> m) {
  current.require("REPARTIDOR");Long key=Long.valueOf(id.substring(2)); var p=orders.findById(key).orElseThrow();
  if(p.getDelivery()==null||!p.getDelivery().getIdUsuario().equals(current.get().getIdUsuario()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  if(List.of("ENTREGADO","CANCELADO").contains(p.getEstadoPedido()))throw new IllegalArgumentException("Esta entrega ya está finalizada");
  String state=switch(text(m,"estado")){case "En camino"->"EN_RUTA";case "Entregado"->"ENTREGADO";default->throw new IllegalArgumentException("Estado de entrega no válido");};
  if("ENTREGADO".equals(state)) {
   String photo=text(m,"evidencia"),signature=text(m,"firma");
   if(!photo.matches("^data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/=\\r\\n]+$")||photo.length()>2100000||!signature.matches("^data:image/png;base64,[A-Za-z0-9+/=]+$")||signature.length()>300000)throw new IllegalArgumentException("Adjunta una fotografía válida y la firma del cliente");
   p.setEvidencia(photo);p.setFirma(signature);
  }
  orderService.actualizarEstadoRepartidor(key,current.get().getIdUsuario(),new PedidoDTO.RequestActualizacionRepartidor(state,text(m,"observaciones")));
 }
}
