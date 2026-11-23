package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Repartidor: ve solo sus pedidos asignados (cliente, dirección y teléfono) y registra la entrega o el incidente.
 * TODO: cuando exista el login, idRepartidor debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/repartidor/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
//Necesario para que permita recibir informacion del fronted por politica de seguridad
@PreAuthorize("hasRole('REPARTIDOR')")
public class RepartidorPedidoController {

    private final PedidoService pedidoService;

    // GET /repartidor/pedidos
    @GetMapping
    public ResponseEntity<List<PedidoDTO.ResponseRepartidor>> listarPedidosAsignados(
            @AuthenticationPrincipal(expression = "idUsuario") Long idRepartidor) {
        return ResponseEntity.ok(pedidoService.listarPedidosAsignados(idRepartidor));
    }

    // PUT /repartidor/pedidos/{idPedido}/estado
    @PutMapping("/{idPedido}/estado")
    public ResponseEntity<PedidoDTO.ResponseRepartidor> actualizarEstadoRepartidor(
            @PathVariable Long idPedido,
            @AuthenticationPrincipal(expression = "idUsuario") Long idRepartidor,
            @Valid @RequestBody PedidoDTO.RequestActualizacionRepartidor dto) {
        return ResponseEntity.ok(pedidoService.actualizarEstadoRepartidor(idPedido, idRepartidor, dto));
    }

    // GET /repartidor/pedidos/historial
    @GetMapping("/historial")
    public ResponseEntity<List<PedidoDTO.ResponseRepartidor>> listarHistorial(
            @AuthenticationPrincipal(expression = "idUsuario") Long idRepartidor) {
        return ResponseEntity.ok(pedidoService.listarHistorialRepartidor(idRepartidor));
    }
}
