package RokerPeru.demo.services;

import RokerPeru.demo.logging.RegistroOperacion;
import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.exception.SolicitudInvalidaException;
import RokerPeru.demo.repository.RolRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Rol que se asigna siempre a quien se registra desde la web pública
    private static final String ROL_CLIENTE = "CLIENTE";

    public AuthDTO.Response login(AuthDTO.LoginRequest request) {
        RegistroOperacion.infoConfirmada(log, "Intentando iniciar sesión para el usuario: {}", request.correoUsuario());
        // 1. El AuthenticationManager verifica que el correo y password (desencriptado) coincidan en la BD.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correoUsuario(), request.passwordUsuario())
        );

        // 2. Si pasamos la línea anterior, las credenciales son correctas. Buscamos al usuario.
        Usuario usuario = usuarioRepository.findByCorreoUsuario(request.correoUsuario()).orElseThrow();

        // 3. Generamos el token con sus datos y su rol (gracias a UserDetails)
        String token = jwtService.generateToken(
                Map.of("rol", usuario.getRol().getNombreRol(),
                        "nombre", usuario.getNombreUsuario(),
                        "idUsuario", usuario.getIdUsuario(),
                        "ver", usuario.getVersionSesion()),
                usuario);
        RegistroOperacion.infoConfirmada(log, "Inicio de sesión exitoso: idUsuario={}, rol={}", usuario.getIdUsuario(), usuario.getRol().getNombreRol());
        // 4. Devolvemos el token envuelto en el DTO
        return new AuthDTO.Response(token);
    }

    @Transactional
    public void register(AuthDTO.RegisterRequest request) {
        RegistroOperacion.infoConfirmada(log, "Registrando nuevo cliente: {}", request.correoUsuario());

        // DNI (8) para NATURAL, RUC (11) para JURIDICA
        int largoEsperado = request.tipoPersonaUsuario().equals("NATURAL") ? 8 : 11;
        if (request.numeroDocumentoUsuario().length() != largoEsperado) {
            throw new SolicitudInvalidaException(largoEsperado == 8
                    ? "Una persona natural debe tener un DNI de 8 dígitos"
                    : "Una persona jurídica debe tener un RUC de 11 dígitos");
        }

        if (usuarioRepository.existsByCorreoUsuarioIgnoreCase(request.correoUsuario())) {
            throw new ConflictoException("Ya existe un usuario con el correo: " + request.correoUsuario());
        }
        if (usuarioRepository.existsByNumeroDocumentoUsuario(request.numeroDocumentoUsuario())) {
            throw new ConflictoException("Ya existe un usuario con el documento: " + request.numeroDocumentoUsuario());
        }

        // El rol NUNCA viene del frontend: siempre CLIENTE
        Rol rolCliente = rolRepository.findByNombreRolIgnoreCase(ROL_CLIENTE)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se puede registrar: el rol " + ROL_CLIENTE + " no existe en la base de datos"));

        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(request.nombreUsuario());
        usuario.setCorreoUsuario(request.correoUsuario());
        usuario.setTipoPersonaUsuario(request.tipoPersonaUsuario());
        usuario.setNumeroDocumentoUsuario(request.numeroDocumentoUsuario());
        usuario.setTelefonoUsuario(request.telefonoUsuario());
        usuario.setDireccionUsuario(request.direccionUsuario());
        usuario.setPasswordUsuario(passwordEncoder.encode(request.passwordUsuario()));
        usuario.setRol(rolCliente);

        usuarioRepository.save(usuario);
        RegistroOperacion.infoConfirmada(log, "Cliente registrado correctamente: {}", usuario.getCorreoUsuario());
    }

    // Cierra la sesión: sube la versión de sesión del usuario y todos sus tokens anteriores dejan de valer.
    // No se guarda ningún token. Importante: solo se acepta un token que SIGA vigente; así un token viejo
    // reenviado no puede cerrar la sesión nueva del mismo usuario.
    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Intento de logout con cabecera 'Authorization' malformada o vacía.");
            return;
        }
        String token = authHeader.substring(7);

        try {
            String correo = jwtService.extractUsername(token); // falla si el token es inválido o ya venció
            Usuario usuario = usuarioRepository.findByCorreoUsuario(correo).orElse(null);

            if (usuario != null && jwtService.isTokenValid(token, usuario)) {
                usuario.setVersionSesion(usuario.getVersionSesion() + 1);
                usuarioRepository.save(usuario);
                RegistroOperacion.infoConfirmada(log, "Sesión cerrada correctamente para el usuario: {}", correo);
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Un token vencido o inválido ya no da acceso: no hay nada que cerrar
            RegistroOperacion.infoConfirmada(log, "Logout ignorado: el token ya no es válido ({})", e.getMessage());
        }
    }
}