package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.services.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión del catálogo de productos.
 */
@RestController
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminProductoController {

    private final ProductoService productoService;

    // POST /admin/productos
    @PostMapping
    public ResponseEntity<ProductoDTO.Response> crearProducto(@Valid @RequestBody ProductoDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(dto));
    }

    // GET /admin/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO.Response>> listarProducto() {
        return ResponseEntity.ok(productoService.listarProducto());
    }

    // GET /admin/productos/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO.Response>> buscarProducto(@RequestParam String texto) {
        return ResponseEntity.ok(productoService.buscarProducto(texto));
    }

    // GET /admin/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.Response> buscarPorIdProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorIdProducto(id));
    }

    // PUT /admin/productos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO.Response> actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoDTO.Request dto) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, dto));
    }

    // PATCH /admin/productos/{id}/estado?estado=...
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProductoDTO.Response> cambiarEstadoProducto(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(productoService.cambiarEstadoProducto(id, estado));
    }

    // DELETE /admin/productos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
