package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
    private String codigoProducto;
    private String nombreProducto;
    private String categoriaProducto;
    private double precioProducto;
    private int stockProducto;
    private String estadoProducto;
    private Long idProveedor;
}