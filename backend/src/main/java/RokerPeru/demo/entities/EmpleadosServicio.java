package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empleados_servicio")
@Getter
@Setter
@NoArgsConstructor
public class EmpleadosServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado_servicio")
    private Long idEmpleadoServicio;

    @Column(name = "nombre_empleado_servicio", nullable = false, length = 100)
    private String nombreEmpleadoServicio;

    @Column(name = "apellido_empleado_servicio", nullable = false, length = 100)
    private String apellidoEmpleadoServicio;

    @Column(name = "telefono_empleado_servicio", length = 20)
    private String telefonoEmpleadoServicio;

    @OneToMany(mappedBy = "empleado")
    private List<Asignaciones> asignaciones = new ArrayList<>();
}