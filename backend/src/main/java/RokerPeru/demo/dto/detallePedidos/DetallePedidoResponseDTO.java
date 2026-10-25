package RokerPeru.demo.dto.detallePedidos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoResponseDTO {
    private Long idDetallePedido;
    private Long idProducto;
    private String nombreProducto;
    private Integer cantidadProducto;
    private double precioUnitarioProducto;
    private double subtotal;
}