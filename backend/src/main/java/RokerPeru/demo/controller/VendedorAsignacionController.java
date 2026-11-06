package RokerPeru.demo.controller;

import RokerPeru.demo.dto.AsignacionDTO;
import RokerPeru.demo.services.AsignacionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: asigna personal de servicio a las órdenes y consulta las asignaciones.
 */
@RestController
@RequestMapping("/vendedor/asignaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorAsignacionController {

    private final AsignacionService asignacionService;

    // POST /vendedor/asignaciones
    @PostMapping
    public ResponseEntity<AsignacionDTO.Response> crearAsignacion(@Valid @RequestBody AsignacionDTO.RequestAsignacion dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asignacionService.crearAsignacion(dto));
    }

    // GET /vendedor/asignaciones
    @GetMapping
    public ResponseEntity<List<AsignacionDTO.Response>> listarTodasAsignaciones() {
        return ResponseEntity.ok(asignacionService.listarTodasAsignaciones());
    }

    // GET /vendedor/asignaciones/orden/{idOrden}
    @GetMapping("/orden/{idOrden}")
    public ResponseEntity<List<AsignacionDTO.Response>> listarAsignacionesPorOrden(@PathVariable Long idOrden) {
        return ResponseEntity.ok(asignacionService.listarAsignacionesPorOrden(idOrden));
    }

    // GET /vendedor/asignaciones/empleado/{idEmpleado}
    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<List<AsignacionDTO.Response>> listarAsignacionesPorEmpleado(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(asignacionService.listarAsignacionesPorEmpleado(idEmpleado));
    }

    // GET /vendedor/asignaciones/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AsignacionDTO.Response> buscarAsignacionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(asignacionService.buscarAsignacionPorId(id));
    }
}
