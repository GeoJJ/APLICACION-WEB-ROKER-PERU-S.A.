package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.services.ProveedorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/almacenista/proveedores")
@RequiredArgsConstructor
public class AlmacenistaProveedorController {

    private final ProveedorService proveedorService;

    // GET /almacenista/proveedores
    @GetMapping
    public List<ProveedorDTO.Response> listarProveedor() {
        return proveedorService.listarProveedor();
    }

    // GET /almacenista/proveedores/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProveedorDTO.Response> buscarProveedor(@RequestParam String texto) {
        return proveedorService.buscarProveedor(texto);
    }
}
