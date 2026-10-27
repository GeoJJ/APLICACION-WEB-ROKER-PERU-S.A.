package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoResponseDTO;
import RokerPeru.demo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/productos")
@RequiredArgsConstructor
public class VendedorProductoController {

    private final ProductoService productoService;

    // GET /vendedor/productos
    @GetMapping
    public List<ProductoResponseDTO> listarProductosVendedor() {
        return productoService.listarProducto();
    }

    // GET /vendedor/productos/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscarProducto(@RequestParam String texto) {
        return productoService.buscarProducto(texto);
    }

    // GET /vendedor/productos/{id}
    @GetMapping("/{id}")
    public ProductoResponseDTO obtenerProductoVendedor(@PathVariable Long id) {
        return productoService.obtenerProductoA(id);
    }
}
