package entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="servicios")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_servicio;

    private String categoria_servicio;
    private String nombre_servicio;
    // private String tipo_servicio;
    private String descripcion_servicio;
    private double precio_servicio;
    private String duracion_estimada_servicio;
    private String cantidad_personal_servicio;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ordenes_Servicio> ordenes_servicio = new ArrayList<>();
}
