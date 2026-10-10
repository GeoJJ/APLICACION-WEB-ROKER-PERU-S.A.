package entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;  
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="usuario")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;

    private String nombre_usuario;

    private String correo_usuario;

    private String tipopersona_usuario;

    private String numero_documento_usuario;

    private String telefono_usuario;

    private String direccion_usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_rol")
    private Rol rol;

    @OneToMany (mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedidos> pedidos = new ArrayList<>();
}
