package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    // Código corto y legible (SRV-0001). Lo genera el backend al crear el servicio; el frontend nunca lo envía.
    @Column(name = "codigo_servicio", length = 20, unique = true)
    private String codigoServicio;

    @Column(name = "categoria_servicio", length = 100)
    private String categoriaServicio;

    @Column(name = "nombre_servicio", nullable = false, length = 150)
    private String nombreServicio;

    @Column(name = "tipo_servicio", length = 50)
    private String tipoServicio;

    @Column(name = "descripcion_servicio", columnDefinition = "TEXT")
    private String descripcionServicio;

    @Column(name = "precio_servicio", nullable = false)
    private double precioServicio;

    @Column(name = "duracion_estimada_servicio", length = 50)
    private String duracionEstimadaServicio;

    @Column(name = "cantidad_personal_servicio", length = 50)
    private String cantidadPersonalServicio;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "servicio")
    private List<OrdenesServicio> ordenesServicio = new ArrayList<>();
}