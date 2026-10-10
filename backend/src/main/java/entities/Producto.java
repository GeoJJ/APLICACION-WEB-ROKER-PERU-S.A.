package entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;  
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="producto")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_producto;

    private String nombre_producto;
    private String categoria_producto;
    private double precio_producto;
    private int stock_producto; 
    private String estado_producto;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Detalle_Pedido> detalle_pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "movimientos_producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movimientos_Producto> movimientos = new ArrayList<>();
    
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="id_proveedor")
    private Proveedores proveedor;
}
