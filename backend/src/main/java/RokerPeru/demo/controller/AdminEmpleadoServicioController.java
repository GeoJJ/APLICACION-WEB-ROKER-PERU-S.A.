package RokerPeru.demo.controller;

import RokerPeru.demo.dto.EmpleadoServicioDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión del personal de servicio (empleados que ejecutan los servicios; no son usuarios del sistema).
 */
@RestController
@RequestMapping("/admin/empleados-servicio")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminEmpleadoServicioController {

    private final EmpleadoServicioService empleadoServicioService;

    // POST /admin/empleados-servicio
    @PostMapping
    public ResponseEntity<EmpleadoServicioDTO.Response> crearEmpleadoServicio(@Valid @RequestBody EmpleadoServicioDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoServicioService.crearEmpleadoServicio(dto));
    }

    // GET /admin/empleados-servicio
    @GetMapping
    public ResponseEntity<List<EmpleadoServicioDTO.Response>> listarEmpleadoServicio() {
        return ResponseEntity.ok(empleadoServicioService.listarEmpleadoServicio());
    }

    // GET /admin/empleados-servicio/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<EmpleadoServicioDTO.Response>> buscarEmpleadoServicio(@RequestParam String texto) {
        return ResponseEntity.ok(empleadoServicioService.buscarEmpleadoServicio(texto));
    }

    // PUT /admin/empleados-servicio/{id}
    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoServicioDTO.Response> actualizarEmpleadoServicio(@PathVariable Long id, @Valid @RequestBody EmpleadoServicioDTO.Request dto) {
        return ResponseEntity.ok(empleadoServicioService.actualizarEmpleadoServicio(id, dto));
    }

    // DELETE /admin/empleados-servicio/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpleadoServicio(@PathVariable Long id) {
        empleadoServicioService.eliminarEmpleadoServicio(id);
        return ResponseEntity.noContent().build();
    }
}
