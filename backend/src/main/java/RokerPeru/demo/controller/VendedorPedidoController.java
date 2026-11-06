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
 * Vendedor: gestiona los pedidos pendientes. Confirma, completa los datos faltantes y asigna al repartidor.
 * TODO: cuando exista el login, idVendedor debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/vendedor/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorPedidoController {

    private final PedidoService pedidoService;

    // GET /vendedor/pedidos
    @GetMapping
    public ResponseEntity<List<PedidoDTO.ResponseVendedor>> listarPedidosParaGestionar() {
        return ResponseEntity.ok(pedidoService.listarPedidosParaGestionar());
    }

    // PUT /vendedor/pedidos/{idPedido}/confirmar?idVendedor=...
    @PutMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoDTO.ResponseVendedor> confirmarYAsignarPedido(@PathVariable Long idPedido, @Valid @RequestBody PedidoDTO.RequestAsignacionVendedor dto, @RequestParam Long idVendedor) {
        return ResponseEntity.ok(pedidoService.confirmarYAsignarPedido(idPedido, dto, idVendedor));
    }
}
