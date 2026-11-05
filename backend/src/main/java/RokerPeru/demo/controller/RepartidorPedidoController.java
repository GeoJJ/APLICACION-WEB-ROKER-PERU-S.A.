package RokerPeru.demo.controller;
import RokerPeru.demo.dto.PedidoDTO; import RokerPeru.demo.services.PedidoService; import RokerPeru.demo.security.CurrentUser;
import jakarta.validation.Valid; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/repartidor/pedidos") @RequiredArgsConstructor
public class RepartidorPedidoController {
 private final PedidoService pedidoService; private final CurrentUser current;
 @GetMapping public List<PedidoDTO.ResponseRepartidor> listar() { return pedidoService.listarAsignadosHistorial(current.get().getIdUsuario()); }
 @GetMapping("/{id}") public PedidoDTO.ResponseRepartidor buscar(@PathVariable Long id) { return pedidoService.buscarRepartidor(id,current.get().getIdUsuario()); }
 @PutMapping("/{id}") public PedidoDTO.ResponseRepartidor actualizar(@PathVariable Long id,@Valid @RequestBody PedidoDTO.RequestActualizacionRepartidor dto) { return pedidoService.actualizarEstadoRepartidor(id,current.get().getIdUsuario(),dto); }
}