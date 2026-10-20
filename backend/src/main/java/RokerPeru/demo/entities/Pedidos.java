package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor

public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "serie_comprobante_pedido", length = 10)
    private String serieComprobantePedido;

    @Column(name = "numero_comprobante_pedido", length = 20)
    private String numeroComprobantePedido;

    @Column(name = "fecha_salida_pedido")
    private LocalDateTime fechaSalidaPedido;

    @Column(name = "fecha_entrega_pedido")
    private LocalDateTime fechaEntregaPedido;

    @Column(name = "incidente_pedido", columnDefinition = "TEXT")
    private String incidentePedido;

    @Column(name = "estado_pedido", nullable = false, length = 30)
    private String estadoPedido;

    @Column(name = "monto_total_pedido", nullable = false)
    private double montoTotalPedido;

    @Column(name = "metodo_pago_pedido", length = 30)
    private String metodoPagoPedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_delivery")
    private Usuario delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auxiliar")
    private Usuario auxiliar;

    // 1 pedido -> N detalles. El dueño de la FK es Detalle_Pedido.pedido.
    // cascade + orphanRemoval: el detalle no existe sin su pedido.    
    // Dependencia fuerte: si se borra un pedido, sus detalles tambien se borran
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Detalle_Pedido> detalle_pedidos = new ArrayList<>();
    // Sincroniza ambos lados, si no la FK queda en NULL.
    public void agregarDetalle(Detalle_Pedido detalle) {
        detalle_pedidos.add(detalle);
        detalle.setPedido(this);
    }

    public void quitarDetalle(Detalle_Pedido detalle) {
        detalle_pedidos.remove(detalle);
        detalle.setPedido(null);
    }
}
