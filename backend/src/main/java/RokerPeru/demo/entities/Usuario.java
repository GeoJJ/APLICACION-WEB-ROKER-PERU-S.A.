package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
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

    @Column(name = "numero_documento_usuario", length = 20)
    private String numeroDocumentoUsuario;

    @Column(name = "telefono_usuario", length = 20)
    private String telefonoUsuario;

    @Column(name = "direccion_usuario", length = 255)
    private String direccionUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @OneToMany(mappedBy = "cliente")
    private List<Pedidos> pedidosComoCliente = new ArrayList<>();

    @OneToMany(mappedBy = "vendedor")
    private List<Pedidos> pedidosComoVendedor = new ArrayList<>();
}