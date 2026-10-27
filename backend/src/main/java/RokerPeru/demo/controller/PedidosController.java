package RokerPeru.demo.controller;

import RokerPeru.demo.dto.pedido.PedidoRequestDTO;
import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import RokerPeru.demo.services.PedidoService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PedidosController {

    private final PedidoService pedidosService;

    // ---------------- OPERACIONES TRANSACCIONALES ----------------

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/crear")
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoRequestDTO request) {
        return ResponseEntity.ok(pedidosService.crearPedido(request));
    }

    @PreAuthorize("hasRole('VENDEDOR')")
    @PutMapping("/confirmar/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> confirmarPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidosService.confirmarPedido(idPedido));
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PutMapping("/cancelar/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidosService.cancelarPedido(idPedido));
    }

    // ---------------- OPERACIONES DE LISTADO GENERAL ----------------

    @PreAuthorize("hasRole('VENDEDOR')")
    @GetMapping("/vendedor")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosVendedor() {
        return ResponseEntity.ok(pedidosService.listarPedidosVendedor());
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(pedidosService.listarPedidosCliente(idCliente));
    }

    @PreAuthorize("hasAnyRole('REPARTIDOR', 'DELIVERY')")
    @GetMapping("/repartidor")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidoRepartidor() {
        return ResponseEntity.ok(pedidosService.listarPedidoRepartidor());
    }

    // ---------------- OPERACIONES DE BÚSQUEDA ESPECÍFICA ----------------

    @PreAuthorize("hasRole('VENDEDOR')")
    @GetMapping("/vendedor/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidosVendedor(@PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidosService.buscarPedidosVendedor(idPedido));
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/cliente/{idCliente}/pedido/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidosCliente(@PathVariable Long idPedido, @PathVariable Long idCliente) {
        return ResponseEntity.ok(pedidosService.buscarPedidosCliente(idPedido, idCliente));
    }

    @PreAuthorize("hasAnyRole('REPARTIDOR', 'DELIVERY')")
    @GetMapping("/repartidor/{idPedido}")
    public ResponseEntity<PedidoResponseDTO> buscarPedidoRepartidor(@PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidosService.buscarPedidoRepartidor(idPedido));
    }
}