package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.services.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/inventario")
@RequiredArgsConstructor
public class VendedorInventarioController {

    private final InventarioService inventarioService;

    // POST /vendedor/inventario/salida
    @PostMapping("/salida")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoDTO.Response registrarSalida(@Valid @RequestBody MovimientoDTO.Request dto) {
        return inventarioService.registrarSalida(dto);
    }

    // GET /vendedor/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public Integer consultarStock(@PathVariable Long idProducto) {
        return inventarioService.consultarStock(idProducto);
    }
}
