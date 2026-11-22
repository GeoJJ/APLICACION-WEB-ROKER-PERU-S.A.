package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.services.OrdenServicioService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: solicita servicios y consulta solo sus órdenes.
 * El idCliente sale siempre del usuario autenticado (token), nunca de un parámetro.
 */
@RestController
@RequestMapping("/cliente/ordenes-servicio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
//Necesario para que permita recibir informacion del fronted por politica de seguridad
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteOrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    // POST /cliente/ordenes-servicio
    @PostMapping
    public ResponseEntity<OrdenServicioDTO.ResponseCliente> crearOrdenServicio(
            @Valid @RequestBody OrdenServicioDTO.RequestCliente dto,
            @AuthenticationPrincipal(expression = "idUsuario") Long idCliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenServicioService.crearOrdenServicio(dto, idCliente));
    }

    // GET /cliente/ordenes-servicio
    @GetMapping
    public ResponseEntity<List<OrdenServicioDTO.ResponseCliente>> listarOrdenesServicioCliente(
            @AuthenticationPrincipal(expression = "idUsuario") Long idCliente) {
        return ResponseEntity.ok(ordenServicioService.listarOrdenesServicioCliente(idCliente));
    }

    // GET /cliente/ordenes-servicio/{idOrden}
    @GetMapping("/{idOrden}")
    public ResponseEntity<OrdenServicioDTO.ResponseCliente> buscarOrdenServicioCliente(
            @PathVariable Long idOrden,
            @AuthenticationPrincipal(expression = "idUsuario") Long idCliente) {
        return ResponseEntity.ok(ordenServicioService.buscarOrdenServicioCliente(idOrden, idCliente));
    }
}