package RokerPeru.demo.controller;

import RokerPeru.demo.services.InventarioService;
import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
import RokerPeru.demo.dto.movimiento.AlertaStockDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
//instalada la librería de Spring Security. Para que Java reconozca la anotación @PreAuthorize
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class InventarioController {

    private final InventarioService inventarioService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    @PostMapping("/ingreso")
    public ResponseEntity<MovimientoResponseDTO> registrarIngreso(@RequestBody MovimientoRequestDTO request) {
        return ResponseEntity.ok(inventarioService.registrarIngreso(request));
    }

    @PreAuthorize("hasRole('VENDEDOR')")
    @PostMapping("/salida")
    public ResponseEntity<MovimientoResponseDTO> registrarSalida(@RequestBody MovimientoRequestDTO request) {
        return ResponseEntity.ok(inventarioService.registrarSalida(request));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoResponseDTO> registrarAjuste(@RequestBody MovimientoRequestDTO request) {
        return ResponseEntity.ok(inventarioService.registrarAjuste(request));
    }

    @PreAuthorize("hasAnyRole('ALMACENISTA', 'VENDEDOR', 'CLIENTE')")
    @GetMapping("/stock/{productoId}")
    public ResponseEntity<Integer> consultarStock(@PathVariable Long productoId) {
        return ResponseEntity.ok(inventarioService.consultarStock(productoId));
    }

    @PreAuthorize("hasAnyRole('GERENTE_GENERAL', 'ADMINISTRADOR', 'ALMACENISTA')")
    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoResponseDTO>> listarMovimientos() {
        return ResponseEntity.ok(inventarioService.listarMovimientos());
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENISTA')")
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaStockDTO>> consultarAlertasStock() {
        return ResponseEntity.ok(inventarioService.consultarAlertasStock());
    }
}