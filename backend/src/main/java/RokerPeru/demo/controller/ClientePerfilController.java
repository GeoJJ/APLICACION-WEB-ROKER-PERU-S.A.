package RokerPeru.demo.controller;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Cliente: consulta y edición de su propio perfil.
 */
@RestController
@RequestMapping("/cliente/perfil")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@PreAuthorize("hasRole('CLIENTE')")
public class ClientePerfilController {

    private final UsuarioService usuarioService;

    // GET /cliente/perfil
    @GetMapping
    public ResponseEntity<UsuarioDTO.PerfilResponse> obtenerPerfil(
            @AuthenticationPrincipal(expression = "idUsuario") Long idCliente) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(idCliente));
    }

    // PUT /cliente/perfil
    @PutMapping
    public ResponseEntity<UsuarioDTO.PerfilResponse> actualizarPerfil(
            @AuthenticationPrincipal(expression = "idUsuario") Long idCliente,
            @Valid @RequestBody UsuarioDTO.PerfilRequest dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(idCliente, dto));
    }
}