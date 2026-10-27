package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import RokerPeru.demo.services.OrdenServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/ordenes-servicio")
@RequiredArgsConstructor
public class VendedorOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // PUT /vendedor/ordenes-servicio/{idOrden}/confirmar
    @PutMapping("/{idOrden}/confirmar")
    public OrdenServicioResponseDTO confirmarOrdenServicio(@PathVariable Long idOrden) {
        return ordenServicioService.confirmarOrdenServicio(idOrden);
    }

    // GET /vendedor/ordenes-servicio
    @GetMapping
    public List<OrdenServicioResponseDTO> listarOrdenesServicioVendedor() {
        return ordenServicioService.listarOrdenesServicioVendedor();
    }

    // GET /vendedor/ordenes-servicio/{idOrden}
    @GetMapping("/{idOrden}")
    public OrdenServicioResponseDTO buscarOrdenServicioVendedor(@PathVariable Long idOrden) {
        return ordenServicioService.buscarOrdenServicioVendedor(idOrden);
    }
}
