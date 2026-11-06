package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.services.InventarioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Gerente general: consulta del historial de movimientos de inventario.
 */
@RestController
@RequestMapping("/gerente/inventario")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GERENTE_GENERAL')")
public class GerenteInventarioController {

    private final InventarioService inventarioService;

    // GET /gerente/inventario/movimientos
    @GetMapping("/movimientos")
    public ResponseEntity<List<MovimientoDTO.Response>> listarMovimientos() {
        return ResponseEntity.ok(inventarioService.listarMovimientos());
    }
}
