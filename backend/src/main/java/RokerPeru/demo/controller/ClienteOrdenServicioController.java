package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.services.OrdenServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: solicita servicios y consulta solo sus órdenes.
 * TODO: cuando exista el login, idCliente debe salir del usuario autenticado y no de un parámetro.
 */
@RestController
@RequestMapping("/cliente/ordenes-servicio")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // POST /cliente/ordenes-servicio
    @PostMapping
    public ResponseEntity<OrdenServicioDTO.ResponseCliente> crearOrdenServicio(@Valid @RequestBody OrdenServicioDTO.RequestCliente dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenServicioService.crearOrdenServicio(dto));
    }

    // GET /cliente/ordenes-servicio?idCliente=...
    @GetMapping
    public ResponseEntity<List<OrdenServicioDTO.ResponseCliente>> listarOrdenesServicioCliente(@RequestParam Long idCliente) {
        return ResponseEntity.ok(ordenServicioService.listarOrdenesServicioCliente(idCliente));
    }

    // GET /cliente/ordenes-servicio/{idOrden}?idCliente=...
    @GetMapping("/{idOrden}")
    public ResponseEntity<OrdenServicioDTO.ResponseCliente> buscarOrdenServicioCliente(@PathVariable Long idOrden, @RequestParam Long idCliente) {
        return ResponseEntity.ok(ordenServicioService.buscarOrdenServicioCliente(idOrden, idCliente));
    }
}
