package RokerPeru.demo.controller;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.services.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrador: gestión de usuarios del sistema.
 */
@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    // POST /admin/usuarios
    @PostMapping
    public ResponseEntity<UsuarioDTO.Response> crearUsuario(@Valid @RequestBody UsuarioDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(dto));
    }

    // GET /admin/usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioDTO.Response>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // GET /admin/usuarios/buscar?texto=...
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO.Response>> buscarUsuario(@RequestParam String texto) {
        return ResponseEntity.ok(usuarioService.buscarUsuario(texto));
    }

    // PUT /admin/usuarios/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO.Response> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO.Request dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }

    // DELETE /admin/usuarios/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
