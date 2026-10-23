package RokerPeru.demo.dto.empleadoServicio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponseDTO {
    private Long idEmpleadoServicio;
    private String nombreEmpleadoServicio;
    private String apellidoEmpleadoServicio;
    private String telefonoEmpleadoServicio;
}