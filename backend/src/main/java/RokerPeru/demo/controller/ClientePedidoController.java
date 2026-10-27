package RokerPeru.demo.controller;

import RokerPeru.demo.dto.pedido.PedidoRequestDTO;
import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente/pedidos")
@RequiredArgsConstructor
public class ClientePedidoController {

    private final PedidoService pedidoService;

    // POST /cliente/pedidos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO crearPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        return pedidoService.crearPedido(dto);
    }

    // PUT /cliente/pedidos/{idPedido}/cancelar
    @PutMapping("/{idPedido}/cancelar")
    public PedidoResponseDTO cancelarPedido(@PathVariable Long idPedido) {
        return pedidoService.cancelarPedido(idPedido);
    }

    // GET /cliente/pedidos?idCliente=...
    @GetMapping
    public List<PedidoResponseDTO> listarPedidosCliente(@RequestParam Long idCliente) {
        return pedidoService.listarPedidosCliente(idCliente);
    }

    // GET /cliente/pedidos/{idPedido}?idCliente=...
    @GetMapping("/{idPedido}")
    public PedidoResponseDTO buscarPedidosCliente(@PathVariable Long idPedido, @RequestParam Long idCliente) {
        return pedidoService.buscarPedidosCliente(idPedido, idCliente);
    }
}
