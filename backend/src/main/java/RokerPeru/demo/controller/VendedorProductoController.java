package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: consulta del catálogo de productos.
 */
@RestController
@RequestMapping("/vendedor/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorProductoController {

    private final ProductoService productoService;

    // GET /vendedor/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO.Response>> listarProducto() {
        return ResponseEntity.ok(productoService.listarProducto());
    }

    // GET /vendedor/productos/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO.Response>> buscarProducto(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.buscarProducto(texto));
    }

    // GET /vendedor/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.Response> buscarPorIdProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorIdProducto(id));
    }
}
