package RokerPeru.demo.services;

import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private static final String CORREO = "cliente@roker.com";
    private static final String PASSWORD = "123456";

    // Verifica que al iniciar sesión con datos correctos se retorne un token JWT.
    @Test
    void login_conCredencialesValidas_retornaToken() {
        // Arrange
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(CORREO, PASSWORD);
        Usuario usuario = mock(Usuario.class);

        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("token-jwt");

        // Act
        AuthDTO.Response response = authService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("token-jwt");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByCorreoUsuario(CORREO);
        verify(jwtService).generateToken(usuario);
    }
    // Comprueba que si la contraseña es incorrecta, se lanza BadCredentialsException sin tocar la BD.
    @Test
    void login_conCredencialesInvalidas_lanzaBadCredentialsException() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(CORREO, "incorrecta");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(usuarioRepository, jwtService);
    }
    // Valida que si el correo no está registrado, se lanza NoSuchElementException.
    @Test
    void login_cuandoUsuarioNoExisteEnBD_lanzaNoSuchElementException() {
        AuthDTO.LoginRequest request = new AuthDTO.LoginRequest(CORREO, PASSWORD);

        when(usuarioRepository.findByCorreoUsuario(CORREO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(NoSuchElementException.class);

        verify(jwtService, never()).generateToken(any());
    }
}