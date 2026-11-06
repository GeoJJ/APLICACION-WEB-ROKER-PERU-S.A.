package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: crea sus pedidos y consulta solo los suyos.
 * TODO: cuando exista el login, idCliente debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/cliente/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClientePedidoController {

    private final PedidoService pedidoService;

    // POST /cliente/pedidos
    @PostMapping
    public ResponseEntity<PedidoDTO.ResponseCliente> crearPedido(@Valid @RequestBody PedidoDTO.RequestCliente dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(dto));
    }

    // GET /cliente/pedidos?idCliente=...
    @GetMapping
    public ResponseEntity<List<PedidoDTO.ResponseCliente>> listarMisPedidos(@RequestParam Long idCliente) {
        return ResponseEntity.ok(pedidoService.listarMisPedidos(idCliente));
    }
}
