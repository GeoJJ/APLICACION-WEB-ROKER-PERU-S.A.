package RokerPeru.demo.controller;

import RokerPeru.demo.dto.usuarios.UsuarioRequestDTO;
import RokerPeru.demo.dto.usuarios.UsuarioResponseDTO;
import RokerPeru.demo.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO crearUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.crearUsuario(dto);
    }

    // GET /api/usuarios
    @GetMapping
    public List<UsuarioResponseDTO> listarUsuario() {
        return usuarioService.listarUsuario();
    }

    // GET /api/usuarios/buscar?texto=juan   (nombre, correo o documento)
    @GetMapping("/buscar")
    public List<UsuarioResponseDTO> buscarUsuario(@RequestParam String texto) {
        return usuarioService.buscarUsuario(texto);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO actualizarUsuario(@PathVariable Long id,
                                                @Valid @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.actualizarUsuario(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}