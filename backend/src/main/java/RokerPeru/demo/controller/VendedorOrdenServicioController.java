package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.services.OrdenServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: gestiona las órdenes de servicio. Para confirmar, la orden debe tener asignado todo el personal que requiere.
 */
@RestController
@RequestMapping("/vendedor/ordenes-servicio")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // GET /vendedor/ordenes-servicio
    @GetMapping
    public ResponseEntity<List<OrdenServicioDTO.ResponseVendedor>> listarOrdenesServicioVendedor() {
        return ResponseEntity.ok(ordenServicioService.listarOrdenesServicioVendedor());
    }

    // GET /vendedor/ordenes-servicio/{idOrden}
    @GetMapping("/{idOrden}")
    public ResponseEntity<OrdenServicioDTO.ResponseVendedor> buscarOrdenServicioVendedor(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenServicioService.buscarOrdenServicioVendedor(idOrden));
    }

    // PUT /vendedor/ordenes-servicio/{idOrden}/confirmar?incidencia=...
    @PutMapping("/{idOrden}/confirmar")
    public ResponseEntity<OrdenServicioDTO.ResponseVendedor> confirmarOrdenServicio(@PathVariable Long idOrden, @RequestParam(required = false) String incidencia) {
        OrdenServicioDTO.RequestGestionVendedor gestion =
                new OrdenServicioDTO.RequestGestionVendedor("CONFIRMADO", incidencia);
        return ResponseEntity.ok(ordenServicioService.confirmarOrdenServicio(idOrden, gestion));
    }

    // PUT /vendedor/ordenes-servicio/{idOrden}/cancelar?incidencia=...
    @PutMapping("/{idOrden}/cancelar")
    public ResponseEntity<OrdenServicioDTO.ResponseVendedor> cancelarOrdenServicio(@PathVariable Long idOrden, @RequestParam(required = false) String incidencia) {
        OrdenServicioDTO.RequestGestionVendedor gestion =
                new OrdenServicioDTO.RequestGestionVendedor("CANCELADO", incidencia);
        return ResponseEntity.ok(ordenServicioService.cancelarOrdenServicio(idOrden, gestion));
    }
}
