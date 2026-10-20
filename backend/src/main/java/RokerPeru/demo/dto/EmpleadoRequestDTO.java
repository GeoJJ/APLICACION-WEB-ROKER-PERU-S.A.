package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequestDTO {
    private String nombreEmpleadoServicio;
    private String apellidoEmpleadoServicio;
    private String telefonoEmpleadoServicio;
    private String cargoEmpleadoServicio;
}