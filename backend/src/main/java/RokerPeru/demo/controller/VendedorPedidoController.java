package RokerPeru.demo.controller;

import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import RokerPeru.demo.services.PedidoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/pedidos")
@RequiredArgsConstructor
public class VendedorPedidoController {

    private final PedidoService pedidoService;

    // PUT /vendedor/pedidos/{idPedido}/confirmar
    @PutMapping("/{idPedido}/confirmar")
    public PedidoResponseDTO confirmarPedido(@PathVariable Long idPedido) {
        return pedidoService.confirmarPedido(idPedido);
    }

    // GET /vendedor/pedidos
    @GetMapping
    public List<PedidoResponseDTO> listarPedidosVendedor() {
        return pedidoService.listarPedidosVendedor();
    }

    // GET /vendedor/pedidos/{idPedido}
    @GetMapping("/{idPedido}")
    public PedidoResponseDTO buscarPedidosVendedor(@PathVariable Long idPedido) {
        return pedidoService.buscarPedidosVendedor(idPedido);
    }
}
