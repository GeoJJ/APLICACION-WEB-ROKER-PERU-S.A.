package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.services.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: catálogo de servicios (vista sin datos internos).
 */
@RestController
@RequestMapping("/cliente/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class ClienteServicioController {

    private final ServicioService servicioService;

    // GET /cliente/servicios
    @GetMapping
    public ResponseEntity<List<ServicioDTO.ClienteResponse>> listarServiciosCliente() {
        return ResponseEntity.ok(servicioService.listarServiciosCliente());
    }

    // GET /cliente/servicios/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<ServicioDTO.ClienteResponse>> buscarServiciosCliente(@RequestParam String texto) {
        return ResponseEntity.ok(servicioService.buscarServiciosCliente(texto));
    }
}
