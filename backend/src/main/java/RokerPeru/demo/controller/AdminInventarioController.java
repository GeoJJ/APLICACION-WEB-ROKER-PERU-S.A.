package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.services.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: ingresos, ajustes, stock, movimientos y alertas.
 */
@RestController
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminInventarioController {

    private final InventarioService inventarioService;

    // POST /admin/inventario/ingreso
    @PostMapping("/ingreso")
    public ResponseEntity<MovimientoDTO.Response> registrarIngreso(@Valid @RequestBody MovimientoDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrarIngreso(dto));
    }

    // POST /admin/inventario/ajuste
    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoDTO.Response> registrarAjuste(@Valid @RequestBody MovimientoDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.registrarAjuste(dto));
    }

    // GET /admin/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public ResponseEntity<Integer> consultarStock(@PathVariable Long idProducto) {
        return ResponseEntity.ok(inventarioService.consultarStock(idProducto));
    }

    // GET /admin/inventario/movimientos
    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDTO.Response>> listarMovimientos() {
        return ResponseEntity.ok(inventarioService.listarMovimientos());
    }

    // GET /admin/inventario/alertas
    @GetMapping("/alertas")
    public ResponseEntity<List<MovimientoDTO.AlertaStockResponse>> consultarAlertasStock() {
        return ResponseEntity.ok(inventarioService.consultarAlertasStock());
    }
}
