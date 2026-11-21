package RokerPeru.demo.services;

import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.TokenInvalido;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.exception.SolicitudInvalidaException;
import RokerPeru.demo.repository.RolRepository;
import RokerPeru.demo.repository.TokenInvalidoRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenInvalidoRepository tokenInvalidoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Rol que se asigna siempre a quien se registra desde la web pública
    private static final String ROL_CLIENTE = "CLIENTE";

    public AuthDTO.Response login(AuthDTO.LoginRequest request) {
        log.info("Intentando iniciar sesión para el usuario: {}", request.correoUsuario());
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
                        "idUsuario", usuario.getIdUsuario()),
                usuario);
        log.info("Inicio de sesión exitoso para el usuario: {} con rol: {}", usuario.getCorreoUsuario(), usuario.getRol().getNombreRol());
        // 4. Devolvemos el token envuelto en el DTO
        return new AuthDTO.Response(token);
    }

    @Transactional
    public void register(AuthDTO.RegisterRequest request) {
        log.info("Registrando nuevo cliente: {}", request.correoUsuario());

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
        log.info("Cliente registrado correctamente: {}", usuario.getCorreoUsuario());
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Validamos que no esté ya registrado para evitar duplicados
            if (tokenInvalidoRepository.findByToken(token).isEmpty()) {
                TokenInvalido tokenInvalido = new TokenInvalido(token, LocalDateTime.now());
                tokenInvalidoRepository.save(tokenInvalido);
                log.info("Sesión cerrada correctamente. Token enviado a la lista negra.");
            } else {
                log.warn("Intento de logout fallido: Cabecera 'Authorization' malformada o vacía.");
            }
        }
    }

    // Verifica si un token ya es invalido
    public boolean isTokenInvalidado(String token) {
        boolean invalidado = tokenInvalidoRepository.findByToken(token).isPresent();
        if (invalidado) {
            log.warn("Se bloqueó una petición: El token presentado está en la lista negra.");
        }
        return invalidado;
    }
}