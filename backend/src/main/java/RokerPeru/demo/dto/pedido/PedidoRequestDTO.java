package RokerPeru.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {
    private String serieComprobantePedido;
    private String numeroComprobantePedido;
    private String metodoPagoPedido;
    private Long idCliente;      // obligatorio
    private Long idVendedor;     // opcional
    private Long idDelivery;     // opcional
    private Long idAuxiliar;     // opcional
    private List<DetallePedidoRequestDTO> detalles;
}