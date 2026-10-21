package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoRequestDTO;
import RokerPeru.demo.dto.ProductoResponseDTO;
import RokerPeru.demo.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("/crear")
    public ProductoResponseDTO crearProducto(@RequestBody ProductoRequestDTO dto) {
        return productoService.crearProducto(dto);
    }

    @GetMapping("/listar")
    public List<ProductoResponseDTO> listarProducto() {
        return productoService.listarProducto();
    }

    @GetMapping("/buscar/{id}")
    public ProductoResponseDTO buscarPorIdProducto(@PathVariable Long id) {
        return productoService.buscarPorIdProducto(id);
    }
}