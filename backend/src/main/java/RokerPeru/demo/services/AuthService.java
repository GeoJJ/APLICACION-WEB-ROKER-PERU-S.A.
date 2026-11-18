package RokerPeru.demo.services;

import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.entities.TokenInvalido;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.TokenInvalidoRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenInvalidoRepository tokenInvalidoRepository;

    public AuthDTO.Response login(AuthDTO.LoginRequest request) {
        log.info("Intentando iniciar sesión para el usuario: {}", request.correoUsuario());
        // 1. El AuthenticationManager verifica que el correo y password (desencriptado) coincidan en la BD.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correoUsuario(), request.passwordUsuario())
        );

        // 2. Si pasamos la línea anterior, las credenciales son correctas. Buscamos al usuario.
        Usuario usuario = usuarioRepository.findByCorreoUsuario(request.correoUsuario()).orElseThrow();

        // 3. Generamos el token con sus datos y su rol (gracias a UserDetails)
        String token = jwtService.generateToken(usuario);
        log.info("Inicio de sesión exitoso para el usuario: {} con rol: {}", usuario.getCorreoUsuario(), usuario.getRol().getNombreRol());
        // 4. Devolvemos el token envuelto en el DTO
        return new AuthDTO.Response(token);
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Validamos que no esté ya registrado para evitar duplicados
            if (tokenInvalidoRepository.findByToken(token).isEmpty()) {
                TokenInvalido tokenInvalido = new TokenInvalido(token, LocalDateTime.now());
                tokenInvalidoRepository.save(tokenInvalido);
                log.info("Sesión cerrada correctamente. Token enviado a la lista negra.");
            }else{
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