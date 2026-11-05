package RokerPeru.demo.config;
import java.util.List;
import RokerPeru.demo.repository.UsuarioRepository;
import RokerPeru.demo.security.RefreshUserFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.*;
@Configuration
public class SecurityConfig {
 @Bean org.springframework.security.core.userdetails.UserDetailsService databaseUsers(UsuarioRepository users) {
  return email -> {
   var u=users.findByCorreoUsuarioIgnoreCase(email).orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
   return org.springframework.security.core.userdetails.User.withUsername(u.getCorreoUsuario()).password(u.getPasswordUsuario()).roles(u.getRol().getNombreRol().toUpperCase(java.util.Locale.ROOT)).disabled(!u.isActivo()).build();
  };
 }
 @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
 @Bean CorsConfigurationSource cors(@Value("${app.cors.origins:http://localhost:5173,http://127.0.0.1:5173}") String origins) {
  var c = new CorsConfiguration(); c.setAllowedOrigins(List.of(origins.split(",")));
  c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("Content-Type","X-CSRF-TOKEN")); c.setAllowCredentials(true);
  c.setExposedHeaders(List.of("Content-Disposition"));
  var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",c); return source;
 }
 @Bean SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource cors, UsuarioRepository users) throws Exception {
  http.cors(c -> c.configurationSource(cors))
   .authorizeHttpRequests(a -> a
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers("/api/auth/csrf","/api/auth/login","/api/auth/register","/error").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/public/**").permitAll()
    .requestMatchers("/api/reportes/**").hasRole("ADMINISTRADOR")
    .requestMatchers(HttpMethod.GET,"/cliente/productos/**","/cliente/servicios/**","/cliente/inventario/**").permitAll()
    .requestMatchers("/api/auth/**","/api/panel/**").authenticated()
    .requestMatchers("/api/checkout/**").hasRole("CLIENTE")
    .requestMatchers("/api/solicitudes/**").authenticated()
    .requestMatchers("/api/admin/**","/admin/**").hasRole("ADMINISTRADOR")
    .requestMatchers("/almacenista/**").hasRole("ALMACENISTA")
    .requestMatchers("/vendedor/**").hasRole("VENDEDOR")
    .requestMatchers("/repartidor/**").hasRole("REPARTIDOR")
    .requestMatchers("/gerente/**").hasRole("ADMINISTRADOR")
    .requestMatchers("/personal/**").hasRole("PERSONAL")
    .requestMatchers("/cliente/**").hasRole("CLIENTE")
    .anyRequest().denyAll())
   .exceptionHandling(e -> e
    .authenticationEntryPoint((req,res,ex) -> { res.setStatus(401); res.setContentType("application/json"); res.getWriter().write("{\"message\":\"Inicia sesión para continuar\"}"); })
    .accessDeniedHandler((req,res,ex) -> { res.setStatus(403); res.setContentType("application/json"); res.getWriter().write("{\"message\":\"No tienes permiso para esta operación o la sesión ha expirado\"}"); }))
   .addFilterBefore(new RefreshUserFilter(users), AuthorizationFilter.class);
  return http.build();
 }
}
