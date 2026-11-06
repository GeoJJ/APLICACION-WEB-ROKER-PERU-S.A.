package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.services.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: salidas de stock (ventas en mostrador) y consulta de disponibilidad.
 */
@RestController
@RequestMapping("/vendedor/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorInventarioController {

    private final InventarioService inventarioService;

    // POST /vendedor/inventario/salida
    @PostMapping("/salida")
    public ResponseEntity<MovimientoDTO.Response> registrarSalida(@Valid @RequestBody MovimientoDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrarSalida(dto));
    }

    // GET /vendedor/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public ResponseEntity<Integer> consultarStock(@PathVariable Long idProducto) {
        return ResponseEntity.ok(inventarioService.consultarStock(idProducto));
    }
}
