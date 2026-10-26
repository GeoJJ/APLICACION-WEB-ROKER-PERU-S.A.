package RokerPeru.demo.controller;

import RokerPeru.demo.dto.UsuarioRequestDTO;
import RokerPeru.demo.dto.UsuarioResponseDTO;
import RokerPeru.demo.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/crear")
    public UsuarioResponseDTO crearUsuario(@RequestBody UsuarioRequestDTO dto) {
        return usuarioService.crearUsuario(dto);
    }

    @GetMapping("/listar")
    public List<UsuarioResponseDTO> listarUsuario() {
        return usuarioService.listarUsuario();
    }

    @PutMapping("/actualizar/{id}")
    public UsuarioResponseDTO actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        return usuarioService.actualizarUsuario(id, dto);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }


    @GetMapping("/buscar/{id}")
    public UsuarioResponseDTO buscarPorIdUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorIdUsuario(id);
    }
}