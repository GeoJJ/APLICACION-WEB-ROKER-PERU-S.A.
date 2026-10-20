package RokerPeru.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PedidoResponseDTO {
    private Long idPedido;
    private LocalDateTime fechaPedido;
    private String serieComprobantePedido;
    private String numeroComprobantePedido;
    private String estadoPedido;
    private double montoTotalPedido;
    private String metodoPagoPedido;
    private String nombreCliente;
    private String nombreVendedor;   // puede ser null
    private List<DetallePedidoResponseDTO> detalles;
}