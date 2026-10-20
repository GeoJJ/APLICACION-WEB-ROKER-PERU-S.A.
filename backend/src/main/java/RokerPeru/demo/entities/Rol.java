package RokerPeru.demo.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "rol")
@Data
@NoArgsConstructor
@Getter
@Setter

public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @OneToMany(mappedBy = "rol")
    @ToString.Exclude // sirve para evitar la recursión infinita al imprimir el objeto Rol, ya que la
                    // lista de usuarios también tiene una referencia al objeto Rol.
    private List<Usuario> usuarios = new ArrayList<>();
}
