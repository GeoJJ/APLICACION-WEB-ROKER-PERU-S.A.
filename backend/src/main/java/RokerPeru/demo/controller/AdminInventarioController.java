package RokerPeru.demo.controller;

import RokerPeru.demo.dto.movimiento.AlertaStockDTO;
import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
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
    public MovimientoResponseDTO registrarIngreso(@Valid @RequestBody MovimientoRequestDTO dto) {
        return inventarioService.registrarIngreso(dto);
    }

    // POST /admin/inventario/ajuste
    @PostMapping("/ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponseDTO registrarAjuste(@Valid @RequestBody MovimientoRequestDTO dto) {
        return inventarioService.registrarAjuste(dto);
    }

    // GET /admin/inventario/movimientos
    @GetMapping("/movimientos")
    public List<MovimientoResponseDTO> listarMovimientos() {
        return inventarioService.listarMovimientos();
    }

    // GET /admin/inventario/alertas
    @GetMapping("/alertas")
    public List<AlertaStockDTO> consultarAlertasStock() {
        return inventarioService.consultarAlertasStock();
    }
}
