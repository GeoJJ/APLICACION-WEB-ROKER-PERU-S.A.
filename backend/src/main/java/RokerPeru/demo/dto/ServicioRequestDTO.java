package RokerPeru.demo.dto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequestDTO {
    @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
    private String categoriaServicio;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombreServicio;

    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    private String tipoServicio;

    private String descripcionServicio;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private Double precioServicio;

    @Size(max = 50, message = "La duración no puede superar los 50 caracteres")
    private String duracionEstimadaServicio;

    @Size(max = 50, message = "La cantidad de personal no puede superar los 50 caracteres")
    private String cantidadPersonalServicio;