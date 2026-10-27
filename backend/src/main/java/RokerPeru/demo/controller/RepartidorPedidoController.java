package RokerPeru.demo.controller;

import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import RokerPeru.demo.services.PedidoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/repartidor/pedidos")
@RequiredArgsConstructor
public class RepartidorPedidoController {

    private final PedidoService pedidoService;

    // GET /repartidor/pedidos
    @GetMapping
    public List<PedidoResponseDTO> listarPedidoRepartidor() {
        return pedidoService.listarPedidoRepartidor();
    }

    // GET /repartidor/pedidos/{idPedido}
    @GetMapping("/{idPedido}")
    public PedidoResponseDTO buscarPedidoRepartidor(@PathVariable Long idPedido) {
        return pedidoService.buscarPedidoRepartidor(idPedido);
    }
}
