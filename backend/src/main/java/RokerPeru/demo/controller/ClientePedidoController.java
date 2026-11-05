package RokerPeru.demo.controller;
import RokerPeru.demo.dto.PedidoDTO; import RokerPeru.demo.services.PedidoService; import RokerPeru.demo.security.CurrentUser;
import jakarta.validation.Valid; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/cliente/pedidos") @RequiredArgsConstructor
public class ClientePedidoController {
 private final PedidoService pedidoService; private final CurrentUser current;
 @PostMapping public PedidoDTO.ResponseCliente crearPedido(@Valid @RequestBody PedidoDTO.RequestCliente dto) { return pedidoService.crearPedido(new PedidoDTO.RequestCliente(dto.metodoPagoPedido(),current.get().getIdUsuario(),dto.detalles())); }
 @GetMapping public List<PedidoDTO.ResponseCliente> listarPedidosCliente() { return pedidoService.listarMisPedidos(current.get().getIdUsuario()); }
 @GetMapping("/{id}") public PedidoDTO.ResponseCliente buscar(@PathVariable Long id) { return pedidoService.buscarCliente(id,current.get().getIdUsuario()); }
 @PutMapping("/{id}/cancelar") public PedidoDTO.ResponseCliente cancelar(@PathVariable Long id) { return pedidoService.cancelarCliente(id,current.get().getIdUsuario()); }
}