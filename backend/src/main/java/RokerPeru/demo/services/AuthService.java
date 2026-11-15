package RokerPeru.demo.services;

import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDTO.Response login(AuthDTO.LoginRequest request) {
        // 1. El AuthenticationManager verifica que el correo y password (desencriptado) coincidan en la BD.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correoUsuario(), request.passwordUsuario())
        );

        // 2. Si pasamos la línea anterior, las credenciales son correctas. Buscamos al usuario.
        Usuario usuario = usuarioRepository.findByCorreoUsuario(request.correoUsuario()).orElseThrow();

        // 3. Generamos el token con sus datos y su rol (gracias a UserDetails)
        String token = jwtService.generateToken(usuario);

        // 4. Devolvemos el token envuelto en el DTO
        return new AuthDTO.Response(token);
    }
}