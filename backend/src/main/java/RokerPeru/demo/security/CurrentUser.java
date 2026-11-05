package RokerPeru.demo.security;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentUser {
    private final UsuarioRepository users;
    public Usuario get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        return users.findByCorreoUsuarioIgnoreCase(auth.getName()).filter(Usuario::isActivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));
    }
    public String role() { return get().getRol().getNombreRol().toUpperCase(java.util.Locale.ROOT); }
    public void require(String... roles) {
        if (!java.util.List.of(roles).contains(role())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
    }
}
