package RokerPeru.demo.services;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.RolRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Transactional
    public UsuarioDTO.ResponseDTO crearUsuario(UsuarioDTO.RequestDTO dto) {
        if (usuarioRepository.existsByCorreoUsuarioIgnoreCase(dto.correoUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el correo: " + dto.correoUsuario());
        }
        if (usuarioRepository.existsByNumeroDocumentoUsuario(dto.numeroDocumentoUsuario())) {
            throw new RuntimeException("Ya existe un usuario con el documento: " + dto.numeroDocumentoUsuario());
        }

        Usuario usuario = new Usuario();
        dto.toEntity(usuario, obtenerRol(dto.idRol()));

        usuario.setPasswordUsuario(passwordEncoder.encode(dto.passwordUsuario()));

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente: {}", guardado.getCorreoUsuario());
        return UsuarioDTO.ResponseDTO.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.ResponseDTO> listarUsuario() {
        return usuarioRepository.findAllByOrderByNombreUsuarioAsc()
                .stream()
                .map(UsuarioDTO.ResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.ResponseDTO> buscarUsuario(String texto) {
        return usuarioRepository.buscarUsuario(texto.trim())
                .stream()
                .map(UsuarioDTO.ResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public UsuarioDTO.ResponseDTO actualizarUsuario(Long id, UsuarioDTO.RequestDTO dto) {
        Usuario usuario = obtenerUsuario(id);

        if (usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(dto.correoUsuario(), id)) {
            throw new RuntimeException("Ya existe otro usuario con el correo: " + dto.correoUsuario());
        }
        if (usuarioRepository.existsByNumeroDocumentoUsuarioAndIdUsuarioNot(dto.numeroDocumentoUsuario(), id)) {
            throw new RuntimeException("Ya existe otro usuario con el documento: " + dto.numeroDocumentoUsuario());
        }

        dto.toEntity(usuario, obtenerRol(dto.idRol()));

        if (dto.passwordUsuario() != null && !dto.passwordUsuario().isBlank()) {
            usuario.setPasswordUsuario(passwordEncoder.encode(dto.passwordUsuario()));
        }

        log.info("Usuario actualizado exitosamente: id={}", id);
        return UsuarioDTO.ResponseDTO.fromEntity(usuario); // Hibernate hace el UPDATE automáticamente
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        // TODO: validar aquí que el usuario no tenga pedidos ni órdenes asociadas
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado: id={}", id);
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    private Rol obtenerRol(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + idRol));
    }
}