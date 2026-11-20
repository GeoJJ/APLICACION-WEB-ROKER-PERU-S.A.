package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.services.PedidoService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: gestiona los pedidos pendientes. Confirma, completa los datos faltantes y asigna al repartidor.
 * TODO: cuando exista el login, idVendedor debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/vendedor/pedidos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
//Necesario para que permita recibir informacion del fronted por politica de seguridad
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorPedidoController {

    private final PedidoService pedidoService;

    // GET /vendedor/pedidos
    @GetMapping
    public ResponseEntity<List<PedidoDTO.ResponseVendedor>> listarPedidosParaGestionar() {
        return ResponseEntity.ok(pedidoService.listarPedidosParaGestionar());
    }

    // PUT /vendedor/pedidos/{idPedido}/confirmar
    @PutMapping("/{idPedido}/confirmar")
    public ResponseEntity<PedidoDTO.ResponseVendedor> confirmarYAsignarPedido(
            @PathVariable Long idPedido,
            @Valid @RequestBody PedidoDTO.RequestAsignacionVendedor dto,
            @AuthenticationPrincipal(expression = "idUsuario") Long idVendedor) {
        return ResponseEntity.ok(pedidoService.confirmarYAsignarPedido(idPedido, dto, idVendedor));
    }

    // GET /vendedor/pedidos/mis-ventas
    @GetMapping("/mis-ventas")
    public ResponseEntity<List<PedidoDTO.ResponseVendedor>> listarMisVentas(@AuthenticationPrincipal Usuario vendedor) {
        return ResponseEntity.ok(pedidoService.listarVentasDelVendedor(vendedor.getIdUsuario()));
    }

    // GET /vendedor/pedidos/ventas?idVendedor=...
    @GetMapping("/ventas")
    public ResponseEntity<List<PedidoDTO.ResponseVendedor>> listarVentas(
            @RequestParam(required = false) Long idVendedor) {
        return ResponseEntity.ok(pedidoService.listarTodasLasVentas(idVendedor));
    }

    // PUT /vendedor/pedidos/{idPedido}
    @PutMapping("/{idPedido}")
    public ResponseEntity<PedidoDTO.ResponseVendedor> editarPedido(
            @PathVariable Long idPedido,
            @Valid @RequestBody PedidoDTO.RequestAsignacionVendedor dto,
            @AuthenticationPrincipal(expression = "idUsuario") Long idVendedor) {
        return ResponseEntity.ok(pedidoService.editarPedidoConfirmado(idPedido, dto, idVendedor));
    }

    // POST /vendedor/pedidos/presencial
    @PostMapping("/presencial")
    public ResponseEntity<PedidoDTO.ResponseVendedor> registrarVentaPresencial(
            @Valid @RequestBody PedidoDTO.RequestCliente dto,
            @RequestParam(required = false) Long idCliente,
            @AuthenticationPrincipal(expression = "idUsuario") Long idVendedor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.registrarVentaPresencial(dto, idCliente, idVendedor));
    }
}
