package RokerPeru.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequestDTO {
    @NotBlank
    private String nombreEmpleadoServicio;
    private String apellidoEmpleadoServicio;
    private String telefonoEmpleadoServicio;
    private String cargoEmpleadoServicio;
}