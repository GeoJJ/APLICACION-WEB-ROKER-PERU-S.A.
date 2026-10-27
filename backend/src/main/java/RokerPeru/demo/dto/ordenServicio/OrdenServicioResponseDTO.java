package RokerPeru.demo.dto.ordenServicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenServicioResponseDTO {
    private Long idOrdenServicio;
    private LocalDateTime fechaOrdenServicio;
    private LocalDateTime fechaProgramadaServicio;
    private String serieComprobanteServicio;
    private String numeroComprobanteServicio;
    private String estadoServicio;
    private double montoTotalServicio;
    private String metodoPagoServicio;
    private String incidenciaServicio;
    
    private String nombreServicio;
    private String nombreCliente;
    private String nombreVendedor;   
    private String nombreAuxiliar;   
}