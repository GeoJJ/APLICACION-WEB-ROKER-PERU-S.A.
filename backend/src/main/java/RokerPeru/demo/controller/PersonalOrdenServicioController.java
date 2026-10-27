package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import RokerPeru.demo.services.OrdenServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/personal/ordenes-servicio")
@RequiredArgsConstructor
public class PersonalOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // GET /personal/ordenes-servicio
    @GetMapping
    public List<OrdenServicioResponseDTO> listarOrdenesServicioPersonal() {
        return ordenServicioService.listarOrdenesServicioPersonal();
    }

    // GET /personal/ordenes-servicio/{idOrden}
    @GetMapping("/{idOrden}")
    public OrdenServicioResponseDTO buscarOrdenServicioPersonal(@PathVariable Long idOrden) {
        return ordenServicioService.buscarOrdenServicioPersonal(idOrden);
    }
}
