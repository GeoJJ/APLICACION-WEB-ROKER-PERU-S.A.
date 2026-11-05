package RokerPeru.demo.services;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
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
    private final RolService rolService; //

    @Transactional
    public UsuarioDTO.Response crearUsuario(UsuarioDTO.Request dto) {
        validarDuplicados(dto.correoUsuario(), dto.numeroDocumentoUsuario(), null);
        Rol rol = rolService.obtenerRol(dto.idRol());
        Usuario usuario = dto.toEntity(rol);

        usuario.setPasswordUsuario(passwordEncoder.encode(dto.passwordUsuario()));

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado exitosamente: {}", guardado.getCorreoUsuario());

        return UsuarioDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.Response> listarUsuarios() {
        return usuarioRepository.findAllByOrderByNombreUsuarioAsc()
                .stream()
                .map(UsuarioDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.Response> buscarUsuario(String texto) {
        return usuarioRepository.buscarUsuario(texto.trim())
                .stream()
                .map(UsuarioDTO.Response::fromEntity)
                .toList();
    }

    @Transactional
    public UsuarioDTO.Response actualizarUsuario(Long id, UsuarioDTO.Request dto) {
        Usuario usuario = obtenerUsuario(id);
        validarDuplicados(dto.correoUsuario(), dto.numeroDocumentoUsuario(), id);

        Rol rol = rolService.obtenerRol(dto.idRol());
        dto.applyTo(usuario, rol);

        if (dto.passwordUsuario() != null && !dto.passwordUsuario().isBlank()) {
            usuario.setPasswordUsuario(passwordEncoder.encode(dto.passwordUsuario()));
        }

        log.info("Usuario actualizado exitosamente: id={}", id);
        return UsuarioDTO.Response.fromEntity(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        // TODO: validar aquí que el usuario no tenga pedidos ni órdenes asociadas
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado: id={}", id);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO.Response> listarRepartidoresDisponibles() {
        // Llamamos al método del repositorio que acabamos de crear
        List<Usuario> repartidores = usuarioRepository.findByRol_NombreRolIgnoreCase("REPARTIDOR");

        // Mapeamos a DTO para enviar solo ID y Nombre al frontend (seguridad y limpieza)
        return repartidores.stream()
                .map(UsuarioDTO.Response::fromEntity)
                .toList();
    }
    // Métodos privados

    private void validarDuplicados(String correo, String documento, Long idIgnorado) {
        boolean correoExiste = (idIgnorado == null)
                ? usuarioRepository.existsByCorreoUsuarioIgnoreCase(correo)
                : usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(correo, idIgnorado);

        if (correoExiste) {
            throw new RuntimeException("Ya existe un usuario con el correo: " + correo);
        }

        boolean docExiste = (idIgnorado == null)
                ? usuarioRepository.existsByNumeroDocumentoUsuario(documento)
                : usuarioRepository.existsByNumeroDocumentoUsuarioAndIdUsuarioNot(documento, idIgnorado);

        if (docExiste) {
            throw new RuntimeException("Ya existe un usuario con el documento: " + documento);
        }
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: id={}", id);
                    return new RuntimeException("Usuario no encontrado: " + id);
                });
    }
}