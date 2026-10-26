package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigoProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombreProducto;

    @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
    private String categoriaProducto;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Double precioProducto;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stockProducto;

    @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
    private String estadoProducto;

    private Long idProveedor;
}