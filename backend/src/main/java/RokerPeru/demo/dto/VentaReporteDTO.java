package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Una fila del "Registro de ventas" (reporte Jasper). Los nombres de los getters
 * deben coincidir con los <field> de registro_ventas.jrxml.
 */
@Getter
@AllArgsConstructor
public class VentaReporteDTO {
    private String fecha;        // dd/MM/yyyy
    private String comprobante;  // B001-00000012
    private String tipo;         // Producto | Servicio
    private String cliente;
    private String documento;    // "DNI 12345678" / "RUC 20123456789"
    private String vendedor;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
}