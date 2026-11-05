package RokerPeru.demo.controller;

import RokerPeru.demo.dto.UsuarioDTO;
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
    public UsuarioDTO.Response crearUsuario(@Valid @RequestBody UsuarioDTO.Request dto) {
        return usuarioService.crearUsuario(dto);
    }

    // GET /admin/usuarios
    @GetMapping
    public List<UsuarioDTO.Response> listarUsuario() {
        return usuarioService.listarUsuarios();
    }

    // GET /admin/usuarios/buscar?texto=...
    @GetMapping("/buscar")
    public List<UsuarioDTO.Response> buscarUsuario(@RequestParam String texto) {
        return usuarioService.buscarUsuario(texto);
    }

    // PUT /admin/usuarios/{id}
    @PutMapping("/{id}")
    public UsuarioDTO.Response actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO.Request dto) {
        return usuarioService.actualizarUsuario(id, dto);
    }

    // DELETE /admin/usuarios/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}
