package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionResponseDTO {
    private Long idAsignacion;
    private String nombreResponsableAsignacion;
    private LocalDateTime fechaAsignacion;
    private String descripcionAsignacion;
    private Long idOrdenServicio;
    private Long idEmpleado;
    private String nombreEmpleado;
}