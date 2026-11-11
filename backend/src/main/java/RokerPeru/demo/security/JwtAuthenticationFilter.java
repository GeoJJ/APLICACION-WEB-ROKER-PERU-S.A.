package RokerPeru.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener la cabecera "Authorization" donde Axios enviará el token
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Si no hay cabecera o no empieza con "Bearer ", ignoramos el token y seguimos (ej. al hacer login)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraemos el token puro (cortamos los primeros 7 caracteres de "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extraemos el correo del token (aquí el JwtService usa la clave secreta)
        userEmail = jwtService.extractUsername(jwt);

        // 5. Si hay un correo y el usuario aún no está autenticado en este hilo
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Buscamos sus datos (incluyendo sus roles) en la BD
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Si el token es matemático y temporalmente válido:
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Creamos el "Pase de Seguridad" oficial de Spring con sus roles
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // GUARDAMOS EL PASE: ¡Ahora Spring Security sabe quién eres y qué roles tienes!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Permitimos que la petición continúe su viaje hacia el Controlador
        filterChain.doFilter(request, response);
    }
}