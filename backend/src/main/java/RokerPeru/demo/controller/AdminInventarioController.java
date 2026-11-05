package RokerPeru.demo.controller;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.services.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class AdminInventarioController {

    private final InventarioService inventarioService;

    // POST /admin/inventario/ingreso
    @PostMapping("/ingreso")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoDTO.Response registrarIngreso(@Valid @RequestBody MovimientoDTO.Request dto) {
        return inventarioService.registrarIngreso(dto);
    }

    // POST /admin/inventario/ajuste
    @PostMapping("/ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoDTO.Response registrarAjuste(@Valid @RequestBody MovimientoDTO.Request dto) {
        return inventarioService.registrarAjuste(dto);
    }

    // GET /admin/inventario/movimientos
    @GetMapping("/movimientos")
    public List<MovimientoDTO.Response> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }

    // GET /admin/inventario/alertas
    @GetMapping("/alertas")
    public List<MovimientoDTO.AlertaStockResponse> consultarAlertasStock() {
        return inventarioService.consultarAlertasStock();
    }
}
