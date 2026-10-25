package RokerPeru.demo.services;

import RokerPeru.demo.dto.UsuarioRequestDTO;
import RokerPeru.demo.dto.UsuarioResponseDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.RolRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + dto.getIdRol()));

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setCorreoUsuario(dto.getCorreoUsuario());
        usuario.setTipoPersonaUsuario(dto.getTipoPersonaUsuario());
        usuario.setNumeroDocumentoUsuario(dto.getNumeroDocumentoUsuario());
        usuario.setTelefonoUsuario(dto.getTelefonoUsuario());
        usuario.setDireccionUsuario(dto.getDireccionUsuario());
        usuario.setRol(rol);

        return aDto(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO buscarPorIdUsuario(Long id) {
        return aDto(usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id)));
    }

    public List<UsuarioResponseDTO> listarUsuario() {
        return usuarioRepository.findAll().stream().map(this::aDto).toList();
    }

    private UsuarioResponseDTO aDto(Usuario u) {
        return new UsuarioResponseDTO(
                u.getIdUsuario(),
                u.getNombreUsuario(),
                u.getCorreoUsuario(),
                u.getTipoPersonaUsuario(),
                u.getNumeroDocumentoUsuario(),
                u.getTelefonoUsuario(),
                u.getDireccionUsuario(),
                u.getRol().getNombreRol()
        );
    }
}