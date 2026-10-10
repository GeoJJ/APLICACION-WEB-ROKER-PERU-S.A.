package entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="ordenes_servicio")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Ordenes_Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_orden_servicio;

    private LocalDateTime fecha_orden_servicio;
    private LocalDateTime fecha_programada_servicio;
    private String serie_comprobante_servicio;
    private String numero_comprobante_servicio;
    private String estado_servicio;
    private double monto_total_servicio;
    private String metodo_pago_servicio;
    private String incidencia_servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_servicio")
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_cliente")
    private Usuario cliente;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_vendedor")
    private Usuario vendedor;

    @OneToMany (mappedBy = "orden_servicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asignaciones> asignaciones = new ArrayList<>();
}
