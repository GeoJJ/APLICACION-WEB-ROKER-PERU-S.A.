package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Almacenista: consulta de productos (solo lectura) para identificar a cuál registrarle movimientos.
 */
@RestController
@RequestMapping("/almacenista/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALMACENISTA')")
public class AlmacenistaProductoController {

    private final ProductoService productoService;

    // GET /almacenista/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO.Response>> listarProducto() {
        return ResponseEntity.ok(productoService.listarProducto());
    }

    // GET /almacenista/productos/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO.Response>> buscarProducto(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.buscarProducto(texto));
    }

    // GET /almacenista/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.Response> buscarPorIdProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorIdProducto(id));
    }
}
