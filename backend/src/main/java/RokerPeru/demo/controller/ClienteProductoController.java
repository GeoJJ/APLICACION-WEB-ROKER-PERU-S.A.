package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoClienteResponseDTO;
import RokerPeru.demo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente/productos")
@RequiredArgsConstructor
public class ClienteProductoController {

    private final ProductoService productoService;

    // GET /cliente/productos
    @GetMapping
    public List<ProductoClienteResponseDTO> listarProductosCliente() {
        return productoService.listarProductosCliente();
    }

    // GET /cliente/productos/{id}
    @GetMapping("/{id}")
    public ProductoClienteResponseDTO obtenerProductoCliente(@PathVariable Long id) {
        return productoService.obtenerProductoCliente(id);
    }
}
