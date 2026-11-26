package RokerPeru.demo.controller;

import RokerPeru.demo.dto.AuthDTO;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String LOGIN_URL = "/auth/login";

    @Test
    void login_conCredencialesValidas_retorna200YToken() throws Exception {
        when(authService.login(any(AuthDTO.LoginRequest.class)))
                .thenReturn(new AuthDTO.Response("token-jwt"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoUsuario": "cliente@roker.com", "passwordUsuario": "123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"));

        verify(authService).login(any(AuthDTO.LoginRequest.class));
    }

    @Test
    void login_conCredencialesInvalidas_retorna401() throws Exception {
        when(authService.login(any(AuthDTO.LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correoUsuario": "cliente@roker.com", "passwordUsuario": "incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conCuerpoVacio_retorna400YNoLlamaAlService() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void login_sinContentType_retorna415() throws Exception {
        mockMvc.perform(post(LOGIN_URL).content("{}"))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(authService);
    }
}
