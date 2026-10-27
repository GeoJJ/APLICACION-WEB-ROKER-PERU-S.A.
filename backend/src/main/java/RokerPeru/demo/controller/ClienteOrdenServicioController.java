package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import RokerPeru.demo.services.OrdenServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente/ordenes-servicio")
@RequiredArgsConstructor
public class ClienteOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // POST /cliente/ordenes-servicio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenServicioResponseDTO crearOrdenServicio(@Valid @RequestBody OrdenServicioRequestDTO dto) {
        return ordenServicioService.crearOrdenServicio(dto);
    }

    // PUT /cliente/ordenes-servicio/{idOrden}/cancelar
    @PutMapping("/{idOrden}/cancelar")
    public OrdenServicioResponseDTO cancelarOrdenServicio(@PathVariable Long idOrden) {
        return ordenServicioService.cancelarOrdenServicio(idOrden);
    }

    // GET /cliente/ordenes-servicio?idCliente=...
    @GetMapping
    public List<OrdenServicioResponseDTO> listarOrdenesServicioCliente(@RequestParam Long idCliente) {
        return ordenServicioService.listarOrdenesServicioCliente(idCliente);
    }

    // GET /cliente/ordenes-servicio/{idOrden}?idCliente=...
    @GetMapping("/{idOrden}")
    public OrdenServicioResponseDTO buscarOrdenServicioCliente(@PathVariable Long idOrden, @RequestParam Long idCliente) {
        return ordenServicioService.buscarOrdenServicioCliente(idOrden, idCliente);
    }
}
