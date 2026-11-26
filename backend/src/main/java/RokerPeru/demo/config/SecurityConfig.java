package RokerPeru.demo.config;

import RokerPeru.demo.logging.RegistroOperacion;
import lombok.extern.slf4j.Slf4j;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Login SIEMPRE público
                        .requestMatchers(HttpMethod.GET, "/cliente/productos/**").permitAll() // las peticiones get en este endpoint pueden entrar sin token
                        .requestMatchers(HttpMethod.GET, "/cliente/servicios/**").permitAll()
                        // Documentación Swagger/OpenAPI: público solo para desarrollo y pruebas con ZAP.
                        // Antes de desplegar a producción, quitar estas rutas o desactivar springdoc.
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated() // CUALQUIER otra ruta EXIGE el Token
                )
                // Le decimos a Spring que no guarde sesiones, usamos JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> {
                            if (request.getDispatcherType() != jakarta.servlet.DispatcherType.ERROR)
                                RegistroOperacion.rechazo(log, "Acceso rechazado: autenticación requerida, método={}, ruta={}", request.getMethod(), request.getRequestURI());
                            new org.springframework.security.web.authentication.Http403ForbiddenEntryPoint().commence(request, response, ex);
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            if (request.getDispatcherType() != jakarta.servlet.DispatcherType.ERROR)
                                RegistroOperacion.rechazo(log, "Acceso denegado: método={}, ruta={}", request.getMethod(), request.getRequestURI());
                            new org.springframework.security.web.access.AccessDeniedHandlerImpl().handle(request, response, ex);
                        }))
                .authenticationProvider(authenticationProvider)
                // Ponemos al guardia JWT en la puerta principal
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
