package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// --- NUEVAS IMPORTACIONES DE SPRING SECURITY ---
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "nombre_usuario", nullable = false, length = 150)
    private String nombreUsuario;

    @Column(name = "correo_usuario", unique = true, length = 150)
    private String correoUsuario;

    @Column(name = "tipopersona_usuario", length = 30)
    private String tipoPersonaUsuario;

    @Column(name = "numero_documento_usuario", nullable = false, unique = true, length = 11)
    private String numeroDocumentoUsuario;

    @Column(name = "telefono_usuario", length = 20)
    private String telefonoUsuario;

    @Column(name = "direccion_usuario", length = 255)
    private String direccionUsuario;

    @Column(name = "password_usuario", nullable = false, length = 100)
    private String passwordUsuario;

    // Versión de la sesión. El token lleva este número; al cerrar sesión sube en 1 y los tokens anteriores dejan de valer.
    // (reemplaza a la tabla de tokens invalidados: no hay que guardar ni limpiar tokens)
    @Column(name = "version_sesion", nullable = false, columnDefinition = "integer default 0")
    private int versionSesion = 0;

    @ManyToOne(fetch = FetchType.EAGER) // <-- Cambiar a EAGER es vital para que Spring lea el rol al iniciar sesión
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @OneToMany(mappedBy = "cliente")
    private List<Pedidos> pedidosComoCliente = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<Pedidos> pedidosComoVendedor = new ArrayList<>();

    // Indica si el usuario está activo; en vez de borrarlo se desactiva para conservar su historial
    @Column(name = "activo", nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    // metodos de seguridad de Spring Security junto JWT
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TRUCO VITAL: Spring Security espera que los roles empiecen con "ROLE_" 
        // cuando usas @PreAuthorize("hasRole(...)")
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol.getNombreRol()));
    }

    @Override
    public String getPassword() {
        return this.passwordUsuario; // Le decimos a Spring dónde está tu contraseña
    }

    @Override
    public String getUsername() {
        return this.correoUsuario; // Le decimos a Spring que usamos el correo para el login
    }

    // Los siguientes 4 métodos validan si la cuenta está activa. 
    // Por ahora los dejamos en 'true' (siempre activos) posteriormente podemos
    // decidir lo que pasa cuando la cuenta expira o se bloquea o cambia permisos
    // o tiene un fallo.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // Spring Security rechaza el login si el usuario está desactivado
    @Override
    public boolean isEnabled() { return activo; }
}