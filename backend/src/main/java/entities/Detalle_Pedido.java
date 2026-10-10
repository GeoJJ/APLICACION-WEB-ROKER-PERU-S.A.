package entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;  
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="detalle_pedido")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Detalle_Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_detalle_pedido;
    private Long id_pedido;

    private Integer cantidad_producto;

    private double precio_unitario_producto;

    private double subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="id_pedido")
    private Pedidos pedido;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_producto")
    private Producto producto;


}
