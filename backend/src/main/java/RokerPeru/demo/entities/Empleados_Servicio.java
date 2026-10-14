package RokerPeru.demo.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity 
@Table(name="empleados_servicio")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Empleados_Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_empleado_servicio;

    private String nombre_empleado_servicio;
    private String apellido_empleado_servicio;
    private String telefono_empleado_servicio;
    private String cargo_empleado_servicio;

    @OneToMany (mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asignaciones> asignaciones = new ArrayList<>();
}
