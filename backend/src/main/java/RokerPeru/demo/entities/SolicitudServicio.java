package RokerPeru.demo.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
@Entity @Table(name="solicitudes_servicio") @Getter @Setter @NoArgsConstructor
public class SolicitudServicio {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_cliente",nullable=false) private Usuario cliente;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_servicio",nullable=false) private Servicio servicio;
 @Column(nullable=false) private LocalDateTime creada;
 @Column(nullable=false) private LocalDateTime programada;
 @Column(nullable=false,length=40) private String estado="PENDIENTE_COTIZACION";
 @Column(length=50) private String ambiente;
 private double area;
 private int ambientes;
 @Column(length=255) private String direccion;
 @Column(length=100) private String distrito;
 @Column(length=255) private String referencia;
 @Column(length=20) private String frecuencia;
 @Column(columnDefinition="TEXT") private String necesidades;
 @Column(length=150) private String nombre;
 @Column(length=20) private String telefono;
 @Column(length=150) private String email;
 private Double precioCotizado;
 @Column(columnDefinition="TEXT") private String observacionesCotizacion;
 @OneToOne(fetch=FetchType.LAZY) private OrdenesServicio orden;
}
