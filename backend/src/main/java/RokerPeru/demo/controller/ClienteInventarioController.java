package RokerPeru.demo.controller;

import RokerPeru.demo.services.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente/inventario")
@RequiredArgsConstructor
public class ClienteInventarioController {

    private final InventarioService inventarioService;

    // GET /cliente/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public Integer consultarStock(@PathVariable Long idProducto) {
        return inventarioService.consultarStock(idProducto);
    }
}
