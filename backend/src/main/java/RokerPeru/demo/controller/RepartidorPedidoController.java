package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Repartidor: ve solo sus pedidos asignados (cliente, dirección y teléfono) y registra la entrega o el incidente.
 * TODO: cuando exista el login, idRepartidor debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/repartidor/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('REPARTIDOR')")
public class RepartidorPedidoController {

    private final PedidoService pedidoService;

    // GET /repartidor/pedidos?idRepartidor=...
    @GetMapping
    public ResponseEntity<List<PedidoDTO.ResponseRepartidor>> listarPedidosAsignados(@RequestParam Long idRepartidor) {
        return ResponseEntity.ok(pedidoService.listarPedidosAsignados(idRepartidor));
    }

    // PUT /repartidor/pedidos/{idPedido}/estado?idRepartidor=...
    @PutMapping("/{idPedido}/estado")
    public ResponseEntity<PedidoDTO.ResponseRepartidor> actualizarEstadoRepartidor(@PathVariable Long idPedido, @RequestParam Long idRepartidor, @Valid @RequestBody PedidoDTO.RequestActualizacionRepartidor dto) {
        return ResponseEntity.ok(pedidoService.actualizarEstadoRepartidor(idPedido, idRepartidor, dto));
    }
}
