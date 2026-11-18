package RokerPeru.demo.security;

import RokerPeru.demo.services.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    // @Lazy rompe el bucle de dependencias con AuthService
    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                   @Lazy AuthService authService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener la cabecera "Authorization" donde Axios enviará el token
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 2. Si no hay cabecera o no empieza con "Bearer ", ignoramos el token y seguimos (ej. al hacer login)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el token puro (cortamos los primeros 7 caracteres de "Bearer ")
        jwt = authHeader.substring(7);

        // Si el token está en la lista negra, no lo procesamos y denegamos acceso ---
        if (authService.isTokenInvalidado(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 4. Si el token no tiene exactamente 2 puntos, no es un JWT: lo ignoramos
        if (jwt.chars().filter(c -> c == '.').count() != 2) {
            logger.warn("Token ignorado: formato JWT inválido");
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Validamos y autenticamos; si algo falla, la petición sigue sin autenticar
        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            logger.warn("Token JWT rechazado: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // 6. Permitimos que la petición continúe su viaje hacia el Controlador
        filterChain.doFilter(request, response);
    }
}