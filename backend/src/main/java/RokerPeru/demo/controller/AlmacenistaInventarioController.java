package RokerPeru.demo.controller;

import RokerPeru.demo.dto.movimiento.AlertaStockDTO;
import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
import RokerPeru.demo.services.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/almacenista/inventario")
@RequiredArgsConstructor
public class AlmacenistaInventarioController {

    private final InventarioService inventarioService;

    // POST /almacenista/inventario/ingreso
    @PostMapping("/ingreso")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarIngreso(@Valid @RequestBody MovimientoRequestDTO dto) {
        return inventarioService.registrarIngreso(dto);
    }

    // POST /almacenista/inventario/ajuste
    @PostMapping("/ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarAjuste(@Valid @RequestBody MovimientoRequestDTO dto) {
        return inventarioService.registrarAjuste(dto);
    }

    // GET /almacenista/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public Integer consultarStock(@PathVariable Long idProducto) {
        return inventarioService.consultarStock(idProducto);
    }

    // GET /almacenista/inventario/movimientos
    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }

    // GET /almacenista/inventario/alertas
    @GetMapping("/alertas")
    public List<AlertaStockDTO> consultarAlertasStock() {
        return inventarioService.consultarAlertasStock();
    }
}
