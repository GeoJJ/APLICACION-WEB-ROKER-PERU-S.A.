package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
public class Asignaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Long idAsignacion;

    @Column(name = "nombre_responsable_asignacion", length = 150)
    private String nombreResponsableAsignacion;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "descripcion_asignacion", columnDefinition = "TEXT")
    private String descripcionAsignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private EmpleadosServicio empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_servicio", nullable = false)
    private OrdenesServicio ordenServicio;
}