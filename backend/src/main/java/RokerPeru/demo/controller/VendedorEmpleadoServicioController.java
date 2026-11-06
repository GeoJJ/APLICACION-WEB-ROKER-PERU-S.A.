package RokerPeru.demo.controller;

import RokerPeru.demo.dto.EmpleadoServicioDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: consulta del personal de servicio para asignarlo a las órdenes.
 */
@RestController
@RequestMapping("/vendedor/empleados-servicio")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorEmpleadoServicioController {

    private final EmpleadoServicioService empleadoServicioService;

    // GET /vendedor/empleados-servicio
    @GetMapping
    public ResponseEntity<List<EmpleadoServicioDTO.Response>> listarEmpleadoServicio() {
        return ResponseEntity.ok(empleadoServicioService.listarEmpleadoServicio());
    }

    // GET /vendedor/empleados-servicio/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<EmpleadoServicioDTO.Response>> buscarEmpleadoServicio(@RequestParam String texto) {
        return ResponseEntity.ok(empleadoServicioService.buscarEmpleadoServicio(texto));
    }
}
