package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.services.ProductoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: catálogo de productos activos (sin stock exacto ni proveedor).
 */
@RestController
@RequestMapping("/cliente/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteProductoController {

    private final ProductoService productoService;

    // GET /cliente/productos
    @GetMapping
    public ResponseEntity<List<ProductoDTO.ClienteResponse>> listarProductosCliente() {
        return ResponseEntity.ok(productoService.listarProductosCliente());
    }

    // GET /cliente/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO.ClienteResponse> obtenerProductoCliente(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductoCliente(id));
    }
}
