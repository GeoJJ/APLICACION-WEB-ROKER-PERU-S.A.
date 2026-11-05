package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.entities.OrdenesServicio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class AsignacionDTO {
    private AsignacionDTO() {
    }

    public record RequestAsignacion(
            @NotNull(message = "El ID de la orden de servicio es obligatorio")
            Long idOrdenServicio,

            @NotNull(message = "El ID del empleado es obligatorio")
            Long idEmpleado,

            @NotBlank(message = "El nombre del responsable es obligatorio")
            String nombreResponsableAsignacion,

            @NotBlank(message = "La descripción de la asignación es obligatoria")
            String descripcionAsignacion
    ) {
        public RequestAsignacion {
            nombreResponsableAsignacion = nombreResponsableAsignacion != null ? nombreResponsableAsignacion.trim() : null;
            descripcionAsignacion = descripcionAsignacion != null ? descripcionAsignacion.trim() : null;
        }

        public Asignaciones toEntity(OrdenesServicio orden, EmpleadosServicio empleado) {
            Asignaciones asignacion = new Asignaciones();
            applyTo(asignacion, orden, empleado);
            return asignacion;
        }

        public void applyTo(Asignaciones asignacion, OrdenesServicio orden, EmpleadosServicio empleado) {
            asignacion.setOrdenServicio(orden);
            asignacion.setEmpleado(empleado);
            asignacion.setNombreResponsableAsignacion(this.nombreResponsableAsignacion);
            asignacion.setDescripcionAsignacion(this.descripcionAsignacion);
            asignacion.setFechaAsignacion(LocalDateTime.now());
        }
    }

    public record Response(
            Long idAsignacion,
            String nombreResponsableAsignacion,
            LocalDateTime fechaAsignacion,
            String descripcionAsignacion,
            Long idOrdenServicio,
            Long idEmpleado,
            String nombreCompletoEmpleado
    ) {
        public static Response fromEntity(Asignaciones a) {
            String nombreCompleto = a.getEmpleado().getNombreEmpleadoServicio() + " " +
                    a.getEmpleado().getApellidoEmpleadoServicio();

            return new Response(
                    a.getIdAsignacion(),
                    a.getNombreResponsableAsignacion(),
                    a.getFechaAsignacion(),
                    a.getDescripcionAsignacion(),
                    a.getOrdenServicio().getIdOrdenServicio(),
                    a.getEmpleado().getIdEmpleadoServicio(),
                    nombreCompleto
            );
        }
    }
}