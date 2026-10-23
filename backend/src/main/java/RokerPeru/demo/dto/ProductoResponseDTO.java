package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private String codigoProducto;
    private String nombreProducto;
    private String categoriaProducto;
    private double precioProducto;
    private int stockProducto;
}
