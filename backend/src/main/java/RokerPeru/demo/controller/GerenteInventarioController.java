package RokerPeru.demo.controller;

import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
import RokerPeru.demo.services.InventarioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gerente/inventario")
@RequiredArgsConstructor
public class GerenteInventarioController {

    private final InventarioService inventarioService;

    // GET /gerente/inventario/movimientos
    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }
}
