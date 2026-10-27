package RokerPeru.demo.dto.movimiento;

import RokerPeru.demo.entities.Producto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
public class AlertaStockDTO {
    private Long productoId;
    private String nombreProducto;
    private Integer stockActual;
    private Integer stockMinimo;
    private String nivelUrgencia;

    public AlertaStockDTO(Producto producto) {
        this.productoId = producto.getIdProducto();
        this.nombreProducto = producto.getNombreProducto();
        this.stockActual = producto.getStockProducto();
        this.stockMinimo = producto.getStockMinimo();

        // Cálculo automático del nivel de urgencia
        if (this.stockActual == 0) {
            this.nivelUrgencia = "AGOTADO - Reponer inmediatamente";
        } else if (this.stockActual <= (this.stockMinimo / 2)) {
            this.nivelUrgencia = "CRÍTICO - Por debajo del 50% del mínimo";
        } else if (this.stockActual <= (this.stockMinimo + (this.stockMinimo/2))) {
            this.nivelUrgencia = "ADVERTENCIA - Acercándose al límite";
        } else{
            this.nivelUrgencia = "NINGUNA - Stock aceptable";
        }
    }
}
