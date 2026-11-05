package RokerPeru.demo.controller;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.CurrentUser;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioRepository users;
    private final PasswordEncoder encoder;
    private final CurrentUser current;
    private final RokerPeru.demo.repository.RolRepository roles;
    private final RokerPeru.demo.services.UsuarioService userService;
    public record Register(@NotBlank String nombre, @NotBlank @Email String email, @NotBlank @Pattern(regexp="\\d{8}") String documento, @NotBlank @Size(min=8,max=72) String password) {}
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody Register dto) {
        var role=roles.findByNombreRolIgnoreCase("CLIENTE").orElseThrow();
        userService.crearUsuario(new RokerPeru.demo.dto.UsuarioDTO.Request(dto.nombre(),dto.email(),"NATURAL",dto.documento(),null,null,role.getIdRol(),dto.password()));
    }
    public record Login(@NotBlank @Email String email, @NotBlank String password) {}
    public record Profile(@NotBlank @Size(max=150) String nombre, @NotBlank @Email String email,
        @Pattern(regexp="^(\\d{9})?$") String telefono, @Size(max=255) String direccion,
        String currentPassword, @Size(max=72) String password) {}
    public static Map<String, Object> view(Usuario u) {
        var m = new LinkedHashMap<String, Object>();
        m.put("id", u.getIdUsuario()); m.put("nombre", u.getNombreUsuario()); m.put("email", u.getCorreoUsuario());
        m.put("telefono", Objects.toString(u.getTelefonoUsuario(), "")); m.put("direccion", Objects.toString(u.getDireccionUsuario(), ""));
        m.put("role", u.getRol().getNombreRol().toUpperCase(Locale.ROOT)); return m;
    }
    @GetMapping("/csrf") public Map<String, String> csrf(CsrfToken token) { return Map.of("token", token.getToken(), "headerName", token.getHeaderName()); }
    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody Login dto, HttpServletRequest request, HttpServletResponse response) {
        var u = users.findByCorreoUsuarioIgnoreCase(dto.email().trim()).orElse(null);
        if (u == null || !u.isActivo() || !encoder.matches(dto.password(), u.getPasswordUsuario()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        request.getSession(true);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(u.getCorreoUsuario(), null,
            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().getNombreRol().toUpperCase(Locale.ROOT)))));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        return view(u);
    }
    @GetMapping("/me") public Map<String,Object> me() { return view(current.get()); }
    @PostMapping("/logout") public void logout(HttpServletRequest request) {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
    }
    @PutMapping("/profile") @Transactional
    public Map<String,Object> profile(@Valid @RequestBody Profile dto, HttpServletRequest request, HttpServletResponse response) {
        var u = users.findById(current.get().getIdUsuario()).orElseThrow();
        String email = dto.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(email, u.getIdUsuario())) throw new IllegalArgumentException("El correo ya está registrado");
        if (!email.equalsIgnoreCase(u.getCorreoUsuario()) || (dto.password() != null && !dto.password().isBlank())) {
            if (dto.currentPassword() == null || !encoder.matches(dto.currentPassword(), u.getPasswordUsuario())) throw new IllegalArgumentException("Confirma tu contraseña actual");
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            if (dto.password().length() < 8) throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            u.setPasswordUsuario(encoder.encode(dto.password()));
        }
        u.setNombreUsuario(dto.nombre().trim()); u.setCorreoUsuario(email); u.setTelefonoUsuario(dto.telefono()); u.setDireccionUsuario(dto.direccion());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().getNombreRol().toUpperCase(Locale.ROOT)))));
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        return view(u);
    }
}
