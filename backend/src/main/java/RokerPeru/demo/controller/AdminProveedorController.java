package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.services.ProveedorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión de proveedores.
 */
@RestController
@RequestMapping("/admin/proveedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminProveedorController {

    private final ProveedorService proveedorService;

    // POST /admin/proveedores
    @PostMapping
    public ResponseEntity<ProveedorDTO.Response> crearProveedor(@Valid @RequestBody ProveedorDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crearProveedor(dto));
    }

    // GET /admin/proveedores
    @GetMapping
    public ResponseEntity<List<ProveedorDTO.Response>> listarProveedor() {
        return ResponseEntity.ok(proveedorService.listarProveedor());
    }

    // GET /admin/proveedores/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTO.Response>> buscarProveedor(@RequestParam String texto) {
        return ResponseEntity.ok(proveedorService.buscarProveedor(texto));
    }

    // PUT /admin/proveedores/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO.Response> actualizarProveedor(@PathVariable Long id, @Valid @RequestBody ProveedorDTO.Request dto) {
        return ResponseEntity.ok(proveedorService.actualizarProveedor(id, dto));
    }

    // DELETE /admin/proveedores/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        proveedorService.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
