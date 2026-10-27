package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoRequestDTO;
import RokerPeru.demo.dto.ProductoResponseDTO;
import RokerPeru.demo.services.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoService productoService;

    // POST /admin/productos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponseDTO crearProducto(@Valid @RequestBody ProductoRequestDTO dto) {
        return productoService.crearProducto(dto);
    }

    // GET /admin/productos
    @GetMapping
    public List<ProductoResponseDTO> listarProductosAdmin() {
        return productoService.listarProducto();
    }

    // GET /admin/productos/buscar?texto=...
    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscarProducto(@RequestParam String texto) {
        return productoService.buscarProducto(texto);
    }

    // GET /admin/productos/{id}
    @GetMapping("/{id}")
    public ProductoResponseDTO obtenerProductoAdmin(@PathVariable Long id) {
        return productoService.obtenerProductoA(id);
    }

    // PUT /admin/productos/{id}
    @PutMapping("/{id}")
    public ProductoResponseDTO actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return productoService.actualizarProducto(id, dto);
    }

    // PATCH /admin/productos/{id}/estado?estado=...
    @PatchMapping("/{id}/estado")
    public ProductoResponseDTO cambiarEstadoProducto(@PathVariable Long id, @RequestParam String estado) {
        return productoService.cambiarEstadoProducto(id, estado);
    }

    // DELETE /admin/productos/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
    }
}
