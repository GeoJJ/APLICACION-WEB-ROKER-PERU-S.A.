package RokerPeru.demo.controller;

import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
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
    public MovimientoResponseDTO registrarSalida(@Valid @RequestBody MovimientoRequestDTO dto) {
        return inventarioService.registrarSalida(dto);
    }

    // GET /vendedor/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public Integer consultarStock(@PathVariable Long idProducto) {
        return inventarioService.consultarStock(idProducto);
    }
}
