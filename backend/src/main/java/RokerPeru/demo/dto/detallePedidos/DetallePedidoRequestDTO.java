package RokerPeru.demo.dto.detallePedidos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequestDTO {
    private Long idProducto;
    private Integer cantidadProducto;
}