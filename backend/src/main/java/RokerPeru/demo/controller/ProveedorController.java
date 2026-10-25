package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorResponseDTO;
import RokerPeru.demo.dto.proveedor.ProveedorRequestDTO;
import RokerPeru.demo.services.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    @PostMapping
    public ProveedorResponseDTO crearProveedor(@Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.crearProveedor(dto);
    }

    @GetMapping
    public List<ProveedorResponseDTO> listarProveedor() {
        return proveedorService.listarProveedor();
    }

    // GET /api/proveedores/buscar?texto=ferre   (por nombre)
    // GET /api/proveedores/buscar?texto=20123   (por RUC)
    @GetMapping("/buscar")
    public List<ProveedorResponseDTO> buscarProveedor(@RequestParam String texto) {
        return proveedorService.buscarProveedor(texto);
    }

    @PutMapping("/{id}")
    public ProveedorResponseDTO actualizarProveedor(@PathVariable Long id,
                                                    @Valid @RequestBody ProveedorRequestDTO dto) {
        return proveedorService.actualizarProveedor(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminarProveedor(@PathVariable Long id) {
        proveedorService.eliminarProveedor(id);
    }
}
