package RokerPeru.demo.dto.movimiento;

import RokerPeru.demo.entities.MovimientosProducto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MovimientoResponseDTO {
    private Long idMovimiento;
    private String nombreProducto;
    private String tipoMovimiento;
    private int cantidadMovida;
    private int stockResultante; 
    private LocalDateTime fechaOperacion;
    private String mensaje;

    // Constructor inteligente para mapeo automático (Opcional, pero recomendado)
    public MovimientoResponseDTO(MovimientosProducto movimiento, int stockResultante, String mensaje) {
        this.idMovimiento = movimiento.getIdMovimientoProducto(); // Ajusta según el ID de tu entidad MovimientosProducto
        this.nombreProducto = movimiento.getProducto().getNombreProducto();
        this.tipoMovimiento = movimiento.getTipoMovimientoProducto();
        this.cantidadMovida = movimiento.getCantidadMovimientoProducto();
        this.fechaOperacion = movimiento.getFechaMovimientoProducto();
        this.stockResultante = stockResultante;
        this.mensaje = mensaje;
    }
}