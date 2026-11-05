package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
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
    public List<ProductoDTO.Response> listarProductosVendedor() {
        return productoService.listarProducto();
    }

    // GET /vendedor/productos/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProductoDTO.Response> buscarProducto(@RequestParam String texto) {
        return productoService.buscarProducto(texto);
    }

    // GET /vendedor/productos/{id}
    @GetMapping("/{id}")
    public ProductoDTO.Response obtenerProductoVendedor(@PathVariable Long id) {
        return productoService.buscarPorIdProducto(id);
    }
}
