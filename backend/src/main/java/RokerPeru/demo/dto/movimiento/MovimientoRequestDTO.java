package RokerPeru.demo.dto.movimiento;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
public class MovimientoRequestDTO {
    private Long productoId;
    private Integer cantidad;
    private String tipoMovimiento;  
    private String justificacion;
}