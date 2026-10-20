package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(name = "codigo_producto", unique = true, length = 50)
    private String codigoProducto;

    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombreProducto;

    @Column(name = "categoria_producto", length = 100)
    private String categoriaProducto;

    @Column(name = "precio_producto", nullable = false)
    private double precioProducto;

    @Column(name = "stock_producto", nullable = false)
    private int stockProducto;

    @Column(name = "estado_producto", length = 30)
    private String estadoProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedores proveedor;

    @OneToMany(mappedBy = "producto")
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    //EN DUDA AUN
    //Es necesario que el movimiento de un producto se borre si se borra el producto?
    //pero este podria mantenerse como para el historial tambien
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientosProducto> movimientos = new ArrayList<>();
}