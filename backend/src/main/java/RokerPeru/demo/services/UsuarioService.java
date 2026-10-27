package RokerPeru.demo.services;

import RokerPeru.demo.dto.UsuarioResponseDTO;
import RokerPeru.demo.dto.usuarios.UsuarioRequestDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.RolRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        String correo = dto.getCorreoUsuario().trim().toLowerCase();
        String tipo = dto.getTipoPersonaUsuario().trim().toUpperCase();
        String documento = dto.getNumeroDocumentoUsuario().trim();

        validarDocumento(tipo, documento);

        if (usuarioRepository.existsByCorreoUsuarioIgnoreCase(correo)) {
            throw new RuntimeException("Ya existe un usuario con el correo: " + correo);
        }
        if (usuarioRepository.existsByNumeroDocumentoUsuario(documento)) {
            throw new RuntimeException("Ya existe un usuario con el documento: " + documento);
        }
        if (dto.getPasswordUsuario() == null || dto.getPasswordUsuario().isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        Usuario usuario = new Usuario();
        copiarDatos(usuario, dto, correo, tipo, documento);
        usuario.setPasswordUsuario(passwordEncoder.encode(dto.getPasswordUsuario()));
        return aDto(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> listarUsuario() {
        return usuarioRepository.findAllByOrderByNombreUsuarioAsc()
                .stream().map(this::aDto).toList();
    }

    public List<UsuarioResponseDTO> buscarUsuario(String texto) {
        return usuarioRepository.buscarUsuario(texto.trim())
                .stream().map(this::aDto).toList();
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = obtenerUsuario(id);

        String correo = dto.getCorreoUsuario().trim().toLowerCase();
        String tipo = dto.getTipoPersonaUsuario().trim().toUpperCase();
        String documento = dto.getNumeroDocumentoUsuario().trim();

        validarDocumento(tipo, documento);

        if (usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(correo, id)) {
            throw new RuntimeException("Ya existe otro usuario con el correo: " + correo);
        }
        if (usuarioRepository.existsByNumeroDocumentoUsuarioAndIdUsuarioNot(documento, id)) {
            throw new RuntimeException("Ya existe otro usuario con el documento: " + documento);
        }

        copiarDatos(usuario, dto, correo, tipo, documento);
        if (dto.getPasswordUsuario() != null && !dto.getPasswordUsuario().isBlank()) {
            usuario.setPasswordUsuario(passwordEncoder.encode(dto.getPasswordUsuario()));
        }
        return aDto(usuario);   // Hibernate hace el UPDATE solo
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);

        // TODO: cuando existan Pedidos y Ordenes_Servicio, validar aquí que el usuario
        // no tenga pedidos ni órdenes asociadas antes de eliminarlo

        usuarioRepository.delete(usuario);
    }

    // ---------- privados ----------

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    private Rol obtenerRol(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + idRol));
    }

    // Regla cruzada: NATURAL -> DNI (8), JURIDICA -> RUC (11)
    private void validarDocumento(String tipo, String documento) {
        if (tipo.equals("NATURAL") && documento.length() != 8) {
            throw new RuntimeException("Una persona natural debe tener un DNI de 8 dígitos");
        }
        if (tipo.equals("JURIDICA") && documento.length() != 11) {
            throw new RuntimeException("Una persona jurídica debe tener un RUC de 11 dígitos");
        }
    }

    // Se usa en crear y actualizar para no repetir los setters
    private void copiarDatos(Usuario usuario, UsuarioRequestDTO dto,
                             String correo, String tipo, String documento) {
        usuario.setNombreUsuario(dto.getNombreUsuario().trim());
        usuario.setCorreoUsuario(correo);
        usuario.setTipoPersonaUsuario(tipo);
        usuario.setNumeroDocumentoUsuario(documento);
        usuario.setTelefonoUsuario(normalizar(dto.getTelefonoUsuario()));
        usuario.setDireccionUsuario(normalizar(dto.getDireccionUsuario()));
        usuario.setRol(obtenerRol(dto.getIdRol()));
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
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
                u.getRol().getIdRol(),
                u.getRol().getNombreRol()
        );
    }
}