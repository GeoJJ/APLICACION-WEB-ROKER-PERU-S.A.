package RokerPeru.demo.controller;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.services.UsuarioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Vendedor: repartidores disponibles para asignarles un pedido.
 */
@RestController
@RequestMapping("/vendedor/repartidores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDEDOR')")
public class VendedorRepartidorController {

    private final UsuarioService usuarioService;

    // GET /vendedor/repartidores
    @GetMapping
    public ResponseEntity<List<UsuarioDTO.Response>> listarRepartidoresDisponibles() {
        return ResponseEntity.ok(usuarioService.listarRepartidoresDisponibles());
    }
}
