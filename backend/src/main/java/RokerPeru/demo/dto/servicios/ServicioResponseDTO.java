package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioResponseDTO {
    private Long idServicio;
    private String categoriaServicio;
    private String nombreServicio;
    private String tipoServicio;
    private String descripcionServicio;
    private double precioServicio;
    private String duracionEstimadaServicio;
    private String cantidadPersonalServicio;
}