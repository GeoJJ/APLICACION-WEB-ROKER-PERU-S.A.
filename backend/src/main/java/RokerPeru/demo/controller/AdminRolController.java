package RokerPeru.demo.controller;

import RokerPeru.demo.dto.RolDTO;
import RokerPeru.demo.services.RolService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión de roles.
 */
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminRolController {

    private final RolService rolService;

    // POST /admin/roles
    @PostMapping
    public ResponseEntity<RolDTO.Response> crearRol(@Valid @RequestBody RolDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crearRol(dto));
    }

    // GET /admin/roles
    @GetMapping
    public ResponseEntity<List<RolDTO.Response>> listarRol() {
        return ResponseEntity.ok(rolService.listarRol());
    }

    // GET /admin/roles/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<RolDTO.Response>> buscarRol(@RequestParam String texto) {
        return ResponseEntity.ok(rolService.buscarRol(texto));
    }

    // PUT /admin/roles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<RolDTO.Response> actualizarRol(@PathVariable Long id, @Valid @RequestBody RolDTO.Request dto) {
        return ResponseEntity.ok(rolService.actualizarRol(id, dto));
    }
}
