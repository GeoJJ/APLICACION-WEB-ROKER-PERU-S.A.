package RokerPeru.demo.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import RokerPeru.demo.repository.UsuarioRepository;

@RequiredArgsConstructor
public class RefreshUserFilter extends OncePerRequestFilter {
    private final UsuarioRepository users;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getPrincipal())) {
            var user = users.findByCorreoUsuarioIgnoreCase(auth.getName()).orElse(null);
            if (user == null || !user.isActivo()) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
            } else {
                var fresh = UsernamePasswordAuthenticationToken.authenticated(user.getCorreoUsuario(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().getNombreRol().toUpperCase(java.util.Locale.ROOT))));
                SecurityContextHolder.getContext().setAuthentication(fresh);
            }
        }
        chain.doFilter(request, response);
    }
}
