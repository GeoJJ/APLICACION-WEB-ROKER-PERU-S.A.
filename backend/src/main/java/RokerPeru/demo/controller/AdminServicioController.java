package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.services.ServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión del catálogo de servicios.
 */
@RestController
@RequestMapping("/admin/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminServicioController {

    private final ServicioService servicioService;

    // POST /admin/servicios
    @PostMapping
    public ResponseEntity<ServicioDTO.Response> crearServicio(@Valid @RequestBody ServicioDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crearServicio(dto));
    }

    // GET /admin/servicios
    @GetMapping
    public ResponseEntity<List<ServicioDTO.Response>> listarServicio() {
        return ResponseEntity.ok(servicioService.listarServicio());
    }

    // GET /admin/servicios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO.Response> buscarPorIdServicio(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.buscarPorIdServicio(id));
    }

    // PUT /admin/servicios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ServicioDTO.Response> actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioDTO.Request dto) {
        return ResponseEntity.ok(servicioService.actualizarServicio(id, dto));
    }

    // DELETE /admin/servicios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
        return ResponseEntity.noContent().build();
    }
}
