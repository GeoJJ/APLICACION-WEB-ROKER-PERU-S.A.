package RokerPeru.demo.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity 
@Table(name="asignaciones")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Asignaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_asignacion;

    private String nombre_responsable_asignacion;
    private String fecha_asignacion;
    private String descripcion_asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_empleado")
    private Empleados_Servicio empleado;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_orden_servicio")
    private Ordenes_Servicio orden_servicio;
}
