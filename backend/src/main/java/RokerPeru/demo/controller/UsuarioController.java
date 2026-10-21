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

    @GetMapping("/buscar/{id}")
    public UsuarioResponseDTO buscarPorIdUsuario(@PathVariable Long id) {
        return usuarioService.buscarPorIdUsuario(id);
    }
}