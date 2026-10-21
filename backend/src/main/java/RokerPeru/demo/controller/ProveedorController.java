package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorRequestDTO;
import RokerPeru.demo.dto.ProveedorResponseDTO;
import RokerPeru.demo.services.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping("/crear")
    public ProveedorResponseDTO crearProveedor(@RequestBody ProveedorRequestDTO dto) {
        return proveedorService.crearProveedor(dto);
    }

    @GetMapping("/listar")
    public List<ProveedorResponseDTO> listarProveedor() {
        return proveedorService.listarProveedor();
    }

    @GetMapping("buscar/{id}")
    public ProveedorResponseDTO buscarPorIdProveedor(@PathVariable Long id) {
        return proveedorService.buscarPorIdProveedor(id);
    }
}
