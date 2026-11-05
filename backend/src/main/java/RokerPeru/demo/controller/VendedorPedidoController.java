package RokerPeru.demo.controller;
import RokerPeru.demo.dto.PedidoDTO; import RokerPeru.demo.services.PedidoService; import RokerPeru.demo.security.CurrentUser;
import jakarta.validation.Valid; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/vendedor/pedidos") @RequiredArgsConstructor
public class VendedorPedidoController {
 private final PedidoService pedidoService; private final CurrentUser current;
 @GetMapping public List<PedidoDTO.ResponseVendedor> listar() { return pedidoService.listarTodos(); }
 @GetMapping("/{id}") public PedidoDTO.ResponseVendedor buscar(@PathVariable Long id) { return pedidoService.buscarVendedor(id); }
 @PutMapping("/{id}/confirmar") public PedidoDTO.ResponseVendedor confirmar(@PathVariable Long id,@Valid @RequestBody PedidoDTO.RequestAsignacionVendedor dto) { return pedidoService.confirmarYAsignarPedido(id,dto,current.get().getIdUsuario()); }
}