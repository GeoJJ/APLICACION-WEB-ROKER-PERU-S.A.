package RokerPeru.demo.dto.ordenServicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenServicioRequestDTO {
    private LocalDateTime fechaProgramadaServicio; // Cuándo se realizará el servicio
    private String serieComprobanteServicio;
    private String numeroComprobanteServicio;
    private String metodoPagoServicio;
    private String incidenciaServicio; // AGREGADO: Descripción del problema inicial por parte del cliente
    
    private Long idServicio; // Obligatorio
    private Long idCliente;  // Obligatorio
    private Long idVendedor; // Opcional
    private Long idAuxiliar; // Opcional
}