package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.services.ProveedorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Almacenista: consulta de proveedores.
 */
@RestController
@RequestMapping("/almacenista/proveedores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALMACENISTA')")
public class AlmacenistaProveedorController {

    private final ProveedorService proveedorService;

    // GET /almacenista/proveedores
    @GetMapping
    public ResponseEntity<List<ProveedorDTO.Response>> listarProveedor() {
        return ResponseEntity.ok(proveedorService.listarProveedor());
    }

    // GET /almacenista/proveedores/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTO.Response>> buscarProveedor(@RequestParam String texto) {
        return ResponseEntity.ok(proveedorService.buscarProveedor(texto));
    }
}
