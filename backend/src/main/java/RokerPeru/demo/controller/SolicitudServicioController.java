package RokerPeru.demo.controller;
import RokerPeru.demo.entities.*;
import RokerPeru.demo.repository.*;
import RokerPeru.demo.security.CurrentUser;
import RokerPeru.demo.services.ComprobanteService;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController @RequestMapping("/api/solicitudes") @RequiredArgsConstructor @Transactional
public class SolicitudServicioController {
 private final CurrentUser current;private final SolicitudServicioRepository requests;private final ServicioRepository services;
 private final OrdenesServicioRepository orders;private final ComprobanteService receipts;
 public record Request(@NotNull Long servicioId,@NotBlank @Pattern(regexp="Hogar|Oficina|Local comercial|Almacén|Otro") String ambiente,
  @NotNull @DecimalMin("1") @DecimalMax("1000000") Double area,@NotNull @Min(1) @Max(10000) Integer ambientes,
  @NotBlank @Size(max=255) String direccion,@NotBlank @Size(max=100) String distrito,@Size(max=255) String referencia,
  @NotNull @Future LocalDateTime programada,@NotBlank @Pattern(regexp="Única|Semanal|Mensual") String frecuencia,
  @NotBlank @Size(max=3000) String necesidades,@NotBlank @Size(max=150) String nombre,
  @NotBlank @Pattern(regexp="\\d{9}") String telefono,@NotBlank @Email @Size(max=150) String email){}
 public record Quote(@NotNull @DecimalMin("0.01") @DecimalMax("10000000") Double precio,@NotNull @Future LocalDateTime programada,@Size(max=3000) String observaciones){}
 @GetMapping @Transactional(readOnly=true) public List<Map<String,Object>> list(){
  current.require("CLIENTE","VENDEDOR","ADMINISTRADOR");var u=current.get();
  return requests.findAll().stream().filter(s->!current.role().equals("CLIENTE")||s.getCliente().getIdUsuario().equals(u.getIdUsuario())).sorted(Comparator.comparing(SolicitudServicio::getCreada).reversed()).map(this::view).toList();
 }
 @PostMapping public Map<String,Object> create(@Valid @RequestBody Request dto){
  current.require("CLIENTE");var service=services.findById(dto.servicioId()).filter(Servicio::isActivo).orElseThrow(()->new IllegalArgumentException("Servicio no disponible"));
  var s=new SolicitudServicio();s.setCliente(current.get());s.setServicio(service);s.setCreada(LocalDateTime.now());s.setProgramada(dto.programada());
  s.setAmbiente(dto.ambiente());s.setArea(dto.area());s.setAmbientes(dto.ambientes());s.setDireccion(dto.direccion());s.setDistrito(dto.distrito());s.setReferencia(dto.referencia());s.setFrecuencia(dto.frecuencia());
  s.setNecesidades(dto.necesidades());s.setNombre(dto.nombre());s.setTelefono(dto.telefono());s.setEmail(dto.email());return view(requests.save(s));
 }
 @PutMapping("/{id}/cotizar") public Map<String,Object> quote(@PathVariable Long id,@Valid @RequestBody Quote dto){
  current.require("VENDEDOR","ADMINISTRADOR");var s=requests.lockById(id).orElseThrow();
  if(!List.of("PENDIENTE_COTIZACION","COTIZADA").contains(s.getEstado()))throw new IllegalArgumentException("La solicitud ya fue atendida");
  s.setPrecioCotizado(dto.precio());s.setProgramada(dto.programada());s.setObservacionesCotizacion(dto.observaciones());s.setEstado("COTIZADA");return view(s);
 }
 @PutMapping("/{id}/aceptar") public Map<String,Object> accept(@PathVariable Long id){
  current.require("CLIENTE");var s=owned(id);
  if("ACEPTADA".equals(s.getEstado()))return view(s);
  if(!"COTIZADA".equals(s.getEstado())||!s.getProgramada().isAfter(LocalDateTime.now()))throw new IllegalArgumentException("La cotización no está disponible o su fecha ya pasó");
  var o=new OrdenesServicio();o.setCliente(s.getCliente());o.setServicio(s.getServicio());o.setFechaOrdenServicio(LocalDateTime.now());o.setFechaProgramadaServicio(s.getProgramada());o.setMontoTotalServicio(s.getPrecioCotizado());o.setEstadoServicio("PENDIENTE");o.setMetodoPagoServicio("POR_COORDINAR");
  var type="JURIDICA".equals(s.getCliente().getTipoPersonaUsuario())?TipoComprobanteEnum.FACTURA_SERVICIO:TipoComprobanteEnum.BOLETA_SERVICIO;
  o.setSerieComprobanteServicio(type.getSerie());o.setNumeroComprobanteServicio(receipts.generarNumeroComprobante(type));
  o.setDetalleSolicitud(s.getAmbiente()+" · "+s.getArea()+" m² · "+s.getAmbientes()+" ambientes · "+s.getFrecuencia()+"\n"+s.getDireccion()+", "+s.getDistrito()+"\n"+Objects.toString(s.getReferencia(),"")+"\n"+s.getNombre()+" · "+s.getTelefono()+"\n"+s.getNecesidades());
  s.setOrden(orders.save(o));s.setEstado("ACEPTADA");return view(s);
 }
 @PutMapping("/{id}/cancelar") public Map<String,Object> cancel(@PathVariable Long id){
  current.require("CLIENTE");var s=owned(id);if(!List.of("PENDIENTE_COTIZACION","COTIZADA").contains(s.getEstado()))throw new IllegalArgumentException("Esta solicitud ya no puede cancelarse");s.setEstado("CANCELADA");return view(s);
 }
 private SolicitudServicio owned(Long id){var s=requests.lockById(id).orElseThrow();if(!s.getCliente().getIdUsuario().equals(current.get().getIdUsuario()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);return s;}
 private Map<String,Object> view(SolicitudServicio s){
  var m=new LinkedHashMap<String,Object>();m.put("id",s.getId());m.put("servicio",s.getServicio().getNombreServicio());m.put("creada",s.getCreada());m.put("programada",s.getProgramada());m.put("estado",s.getEstado());
  m.put("ambiente",s.getAmbiente());m.put("area",s.getArea());m.put("ambientes",s.getAmbientes());m.put("direccion",s.getDireccion());m.put("distrito",s.getDistrito());m.put("referencia",s.getReferencia());m.put("frecuencia",s.getFrecuencia());m.put("necesidades",s.getNecesidades());
  m.put("nombre",s.getNombre());m.put("telefono",s.getTelefono());m.put("email",s.getEmail());m.put("precio",s.getPrecioCotizado());m.put("observaciones",s.getObservacionesCotizacion());m.put("ordenId",s.getOrden()==null?null:s.getOrden().getIdOrdenServicio());return m;
 }
}
