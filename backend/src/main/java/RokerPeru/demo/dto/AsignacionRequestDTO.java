package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRequestDTO {
    private Long idOrdenServicio;
    private Long idEmpleado;
    private String nombreResponsableAsignacion;
    private String descripcionAsignacion;
}