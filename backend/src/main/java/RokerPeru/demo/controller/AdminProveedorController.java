package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorResponseDTO;
import RokerPeru.demo.dto.proveedor.ProveedorRequestDTO;
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
    public ProveedorResponseDTO crearProveedor(@Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.crearProveedor(dto);
    }

    // GET /admin/proveedores
    @GetMapping
    public List<ProveedorResponseDTO> listarProveedor() {
        return proveedorService.listarProveedor();
    }

    // GET /admin/proveedores/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProveedorResponseDTO> buscarProveedor(@RequestParam String texto) {
        return proveedorService.buscarProveedor(texto);
    }

    // PUT /admin/proveedores/{id}
    @PutMapping("/{id}")
    public ProveedorResponseDTO actualizarProveedor(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.actualizarProveedor(id, dto);
    }

    // DELETE /admin/proveedores/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProveedor(@PathVariable Long id) {
        proveedorService.eliminarProveedor(id);
    }
}
