package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
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
    public MovimientoDTO.Response registrarIngreso(@Valid @RequestBody MovimientoDTO.Request dto) {
        return inventarioService.registrarIngreso(dto);
    }

    // POST /almacenista/inventario/ajuste
    @PostMapping("/ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoDTO.Response registrarAjuste(@Valid @RequestBody MovimientoDTO.Request dto) {
        return inventarioService.registrarAjuste(dto);
    }

    // GET /almacenista/inventario/stock/{idProducto}
    @GetMapping("/stock/{idProducto}")
    public Integer consultarStock(@PathVariable Long idProducto) {
        return inventarioService.consultarStock(idProducto);
    }

    // GET /almacenista/inventario/movimientos
    @GetMapping("/movimientos")
    public List<MovimientoDTO.Response> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }

    // GET /almacenista/inventario/alertas
    @GetMapping("/alertas")
    public List<MovimientoDTO.AlertaStockResponse> consultarAlertasStock() {
        return inventarioService.consultarAlertasStock();
    }
}
