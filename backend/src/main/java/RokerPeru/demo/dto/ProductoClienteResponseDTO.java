package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoClienteResponseDTO {
    private String nombreProducto;
    private String categoriaProducto;
    private double precioProducto;
    private boolean disponible;
}
