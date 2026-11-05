package RokerPeru.demo.controller;
import RokerPeru.demo.dto.*;
import RokerPeru.demo.repository.*;
import RokerPeru.demo.security.CurrentUser;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/checkout") @RequiredArgsConstructor
public class CheckoutController {
 private final CurrentUser current;private final UsuarioRepository users;private final PedidosRepository orders;private final PedidoService service;
 public record Item(@NotNull Long idProducto,@NotNull @Min(1) @Max(10000) Integer cantidadProducto){}
 public record Request(@NotBlank @Size(max=80) String key,@NotBlank @Size(max=150) String nombre,
  @NotBlank @Pattern(regexp="\\d{9}") String telefono,@NotBlank @Size(max=255) String direccion,
  @NotBlank @Size(max=100) String distrito,@Size(max=100) String referencia,
  @NotBlank @Pattern(regexp="EFECTIVO|TRANSFERENCIA") String metodoPago,@NotEmpty @Size(max=100) List<@Valid Item> items){}
 public record Receipt(Long id,String comprobante,double total,String estado){}
 @PostMapping @Transactional
 public Receipt checkout(@Valid @RequestBody Request dto){
  current.require("CLIENTE");var user=users.lockById(current.get().getIdUsuario()).orElseThrow();
  String key=user.getIdUsuario()+":"+dto.key();var existing=orders.findByCheckoutKey(key);
  if(existing.isPresent())return receipt(existing.get());
  var grouped=new TreeMap<Long,Integer>();for(var item:dto.items())grouped.merge(item.idProducto(),item.cantidadProducto(),Math::addExact);
  var details=grouped.entrySet().stream().map(e->new DetallePedidoDTO.Request(e.getKey(),e.getValue())).toList();
  var result=service.crearPedido(new PedidoDTO.RequestCliente(dto.metodoPago(),user.getIdUsuario(),details));
  var order=orders.findById(result.idPedido()).orElseThrow();
  order.setCheckoutKey(key);order.setContactoEntrega(dto.nombre().trim());order.setTelefonoEntrega(dto.telefono());
  order.setDireccionEntrega(dto.direccion().trim()+", "+dto.distrito().trim()+(dto.referencia()==null||dto.referencia().isBlank()?"":". Referencia: "+dto.referencia().trim()));
  return receipt(order);
 }
 private Receipt receipt(RokerPeru.demo.entities.Pedidos p){return new Receipt(p.getIdPedido(),p.getNumeroComprobantePedido(),p.getMontoTotalPedido(),p.getEstadoPedido());}
}
