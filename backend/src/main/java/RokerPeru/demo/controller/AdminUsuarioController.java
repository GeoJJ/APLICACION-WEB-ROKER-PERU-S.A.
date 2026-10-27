package RokerPeru.demo.controller;

import RokerPeru.demo.dto.usuarios.UsuarioRequestDTO;
import RokerPeru.demo.dto.usuarios.UsuarioResponseDTO;
import RokerPeru.demo.services.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    // POST /admin/usuarios
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO crearUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.crearUsuario(dto);
    }

    // GET /admin/usuarios
    @GetMapping
    public List<UsuarioResponseDTO> listarUsuario() {
        return usuarioService.listarUsuario();
    }

    // GET /admin/usuarios/buscar?texto=...
    @GetMapping("/buscar")
    public List<UsuarioResponseDTO> buscarUsuario(@RequestParam String texto) {
        return usuarioService.buscarUsuario(texto);
    }

    // PUT /admin/usuarios/{id}
    @PutMapping("/{id}")
    public UsuarioResponseDTO actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.actualizarUsuario(id, dto);
    }

    // DELETE /admin/usuarios/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}
