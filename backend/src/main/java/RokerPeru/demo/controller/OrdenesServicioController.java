package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import RokerPeru.demo.services.OrdenServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-servicio")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OrdenesServicioController {

    private final OrdenServicioService ordenesServicioService;

    // ---------------- OPERACIONES TRANSACCIONALES ----------------

    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/crear")
    public ResponseEntity<OrdenServicioResponseDTO> crearOrdenServicio(@RequestBody OrdenServicioRequestDTO request) {
        return ResponseEntity.ok(ordenesServicioService.crearOrdenServicio(request));
    }

    @PreAuthorize("hasRole('VENDEDOR')")
    @PutMapping("/confirmar/{idOrden}")
    public ResponseEntity<OrdenServicioResponseDTO> confirmarOrdenServicio(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenesServicioService.confirmarOrdenServicio(idOrden));
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @PutMapping("/cancelar/{idOrden}")
    public ResponseEntity<OrdenServicioResponseDTO> cancelarOrdenServicio(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenesServicioService.cancelarOrdenServicio(idOrden));
    }

    // ---------------- OPERACIONES DE LISTADO GENERAL ----------------

    @PreAuthorize("hasRole('VENDEDOR')")
    @GetMapping("/vendedor")
    public ResponseEntity<List<OrdenServicioResponseDTO>> listarOrdenesServicioVendedor() {
        return ResponseEntity.ok(ordenesServicioService.listarOrdenesServicioVendedor());
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<OrdenServicioResponseDTO>> listarOrdenesServicioCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(ordenesServicioService.listarOrdenesServicioCliente(idCliente));
    }

    @PreAuthorize("hasRole('PERSONAL_SERVICIO')")
    @GetMapping("/personal")
    public ResponseEntity<List<OrdenServicioResponseDTO>> listarOrdenesServicioPersonal() {
        return ResponseEntity.ok(ordenesServicioService.listarOrdenesServicioPersonal());
    }

    // ---------------- OPERACIONES DE BÚSQUEDA ESPECÍFICA ----------------

    @PreAuthorize("hasRole('VENDEDOR')")
    @GetMapping("/vendedor/{idOrden}")
    public ResponseEntity<OrdenServicioResponseDTO> buscarOrdenServicioVendedor(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenesServicioService.buscarOrdenServicioVendedor(idOrden));
    }

    @PreAuthorize("hasRole('CLIENTE')")
    @GetMapping("/cliente/{idCliente}/orden/{idOrden}")
    public ResponseEntity<OrdenServicioResponseDTO> buscarOrdenServicioCliente(@PathVariable Long idOrden, @PathVariable Long idCliente) {
        return ResponseEntity.ok(ordenesServicioService.buscarOrdenServicioCliente(idOrden, idCliente));
    }

    @PreAuthorize("hasRole('PERSONAL_SERVICIO')")
    @GetMapping("/personal/{idOrden}")
    public ResponseEntity<OrdenServicioResponseDTO> buscarOrdenServicioPersonal(@PathVariable Long idOrden) {
        return ResponseEntity.ok(ordenesServicioService.buscarOrdenServicioPersonal(idOrden));
    }
}