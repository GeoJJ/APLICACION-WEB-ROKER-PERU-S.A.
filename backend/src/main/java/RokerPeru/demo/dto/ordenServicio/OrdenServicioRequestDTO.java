package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenServicioRequestDTO {
    private LocalDateTime fechaProgramadaServicio;
    private String serieComprobanteServicio;
    private String numeroComprobanteServicio;
    private String metodoPagoServicio;
    private Long idServicio;
    private Long idCliente;
    private Long idVendedor;
    private Long idAuxiliar;
}