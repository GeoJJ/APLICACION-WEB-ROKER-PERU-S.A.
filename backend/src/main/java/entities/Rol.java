package entities;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="rol")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_rol;

    private String nombre_rol;

    @OneToMany(mappedBy = "rol", cascade = CascadeType.ALL)
    @ToString.Exclude //sirve para evitar la recursión infinita al imprimir el objeto Rol, ya que la lista de usuarios también tiene una referencia al objeto Rol.
    private List<Usuario> usuarios = new ArrayList<>();
}
