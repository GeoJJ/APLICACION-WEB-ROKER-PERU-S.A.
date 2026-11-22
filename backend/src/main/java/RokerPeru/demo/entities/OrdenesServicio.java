package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_servicio", uniqueConstraints = @UniqueConstraint(
        name = "uk_ordenes_servicio_comprobante",
        columnNames = {"serie_comprobante_servicio", "numero_comprobante_servicio"}))
@Getter
@Setter
@NoArgsConstructor
public class OrdenesServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_servicio")
    private Long idOrdenServicio;

    // Código corto y legible (ORD-0001). Lo genera el backend al crear la orden; el frontend nunca lo envía.
    @Column(name = "codigo_orden_servicio", length = 20, unique = true)
    private String codigoOrdenServicio;

    @Column(name = "fecha_orden_servicio", nullable = false)
    private LocalDateTime fechaOrdenServicio;

    @Column(name = "fecha_programada_servicio")
    private LocalDateTime fechaProgramadaServicio;

    @Column(name = "serie_comprobante_servicio", length = 10)
    private String serieComprobanteServicio;

    @Column(name = "numero_comprobante_servicio", length = 20)
    private String numeroComprobanteServicio;

    @Column(name = "fecha_emision_comprobante")
    private LocalDateTime fechaEmisionComprobante;

    @Column(name = "estado_servicio", nullable = false, length = 30)
    private String estadoServicio;

    @Column(name = "monto_total_servicio", nullable = false)
    private double montoTotalServicio;

    @Column(name = "metodo_pago_servicio", length = 30)
    private String metodoPagoServicio;

    // Notas del vendedor al confirmar / completar / cancelar
    @Column(name = "incidencia_servicio", columnDefinition = "TEXT")
    private String incidenciaServicio;

    // Lo que el cliente indica al solicitar el servicio (dónde y qué necesita)
    @Column(name = "direccion_servicio", length = 255)
    private String direccionServicio;

    @Column(name = "detalle_solicitud_servicio", columnDefinition = "TEXT")
    private String detalleSolicitudServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auxiliar")
    private Usuario auxiliar;

    @OneToMany(mappedBy = "ordenServicio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asignaciones> asignaciones = new ArrayList<>();
}