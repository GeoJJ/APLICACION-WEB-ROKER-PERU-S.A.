package RokerPeru.demo.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="pedidos")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_pedido;

    private LocalDateTime fecha_pedido;
    private String serie_comprobante_pedido;
    private String numero_comprobante_pedido;
    private LocalDateTime fecha_salida_pedido;
    private LocalDateTime fecha_entrega_pedido;
    private String incidente_pedido;
    private String estado_pedido;
    private double monto_total_pedido;
    private String metodo_pago_pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_cliente")
    private Usuario cliente;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_vendedor")
    private Usuario vendedor;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_delivery")
    private Usuario delivery;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_auxiliar")
    private Usuario auxiliar;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Detalle_Pedido> detalle_pedidos = new ArrayList<>();
}
