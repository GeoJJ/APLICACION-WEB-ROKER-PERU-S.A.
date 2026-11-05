package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.services.ProveedorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/proveedores")
@RequiredArgsConstructor
public class AdminProveedorController {

    private final ProveedorService proveedorService;

    // POST /api/admin/proveedores
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProveedorDTO.Response crearProveedor(@Valid @RequestBody ProveedorDTO.Request dto) {
        return proveedorService.crearProveedor(dto);
    }

    // GET /admin/proveedores
    @GetMapping
    public List<ProveedorDTO.Response> listarProveedor() {
        return proveedorService.listarProveedor();
    }

    // GET /admin/proveedores/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProveedorDTO.Response> buscarProveedor(@RequestParam String texto) {
        return proveedorService.buscarProveedor(texto);
    }

    // PUT /admin/proveedores/{id}
    @PutMapping("/{id}")
    public ProveedorDTO.Response actualizarProveedor(@PathVariable Long id, @Valid @RequestBody ProveedorDTO.Request dto) {
        return proveedorService.actualizarProveedor(id, dto);
    }

    // DELETE /admin/proveedores/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProveedor(@PathVariable Long id) {
        proveedorService.eliminarProveedor(id);
    }
}
