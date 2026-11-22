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

    // Código corto y legible (PRD-0001). Lo genera el backend al crear el producto; el frontend nunca lo envía.
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

    // Nuevo campo agregado es para la alerta de stock_minimo
    @Column(name = "stock_minimo", nullable = false, columnDefinition = "integer default 10")
    private int stockMinimo = 10;

    @Column(name = "estado_producto", length = 30)
    private String estadoProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor")
    private Proveedores proveedor;

    @OneToMany(mappedBy = "producto")
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<MovimientosProducto> movimientos = new ArrayList<>();
}