package RokerPeru.demo.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioClienteResponseDTO {
    String categoriaServicio;
    String nombreServicio;
    String tipoServicio;
    String descripcionServicio;
    double precioServicio;
    String duracionEstimadaServicio;
    String cantidadPersonalServicio;
}
