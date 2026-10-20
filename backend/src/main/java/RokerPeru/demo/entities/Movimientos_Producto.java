package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_producto")
@Getter
@Setter
@NoArgsConstructor
public class Movimientos_Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento_producto")
    private Long idMovimientoProducto;

    @Column(name = "tipo_movimiento_producto", nullable = false, length = 30)
    private String tipoMovimientoProducto;

    @Column(name = "cantidad_movimiento_producto", nullable = false)
    private int cantidadMovimientoProducto;

    @Column(name = "fecha_movimiento_producto", nullable = false)
    private LocalDateTime fechaMovimientoProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}