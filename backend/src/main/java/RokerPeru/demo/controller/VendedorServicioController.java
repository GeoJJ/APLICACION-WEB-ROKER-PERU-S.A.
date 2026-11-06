package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.services.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: consulta del catálogo de servicios.
 */
@RestController
@RequestMapping("/vendedor/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorServicioController {

    private final ServicioService servicioService;

    // GET /vendedor/servicios
    @GetMapping
    public ResponseEntity<List<ServicioDTO.Response>> listarServicio() {
        return ResponseEntity.ok(servicioService.listarServicio());
    }

    // GET /vendedor/servicios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ServicioDTO.Response> buscarPorIdServicio(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.buscarPorIdServicio(id));
    }
}
