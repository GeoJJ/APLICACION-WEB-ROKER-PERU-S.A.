package RokerPeru.demo.dto;

import RokerPeru.demo.entities.EmpleadosServicio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class EmpleadoServicioDTO {
    private EmpleadoServicioDTO() {
    }

    public record Request(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            String nombreEmpleadoServicio,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
            String apellidoEmpleadoServicio,

            @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos")
            String telefonoEmpleadoServicio
    ) {
        public Request {
            nombreEmpleadoServicio = nombreEmpleadoServicio != null ? nombreEmpleadoServicio.trim().toUpperCase() : null;
            apellidoEmpleadoServicio = apellidoEmpleadoServicio != null ? apellidoEmpleadoServicio.trim().toUpperCase() : null;
            telefonoEmpleadoServicio = (telefonoEmpleadoServicio == null || telefonoEmpleadoServicio.isBlank()) ? null : telefonoEmpleadoServicio.trim();
        }

        public EmpleadosServicio toEntity() {
            EmpleadosServicio empleado = new EmpleadosServicio();
            applyTo(empleado);
            return empleado;
        }

        public void applyTo(EmpleadosServicio empleado) {
            empleado.setNombreEmpleadoServicio(this.nombreEmpleadoServicio);
            empleado.setApellidoEmpleadoServicio(this.apellidoEmpleadoServicio);
            empleado.setTelefonoEmpleadoServicio(this.telefonoEmpleadoServicio);
        }
    }

    public record Response(
            Long idEmpleadoServicio,
            String nombreEmpleadoServicio,
            String apellidoEmpleadoServicio,
            String telefonoEmpleadoServicio
    ) {
        public static Response fromEntity(EmpleadosServicio e) {
            return new Response(
                    e.getIdEmpleadoServicio(),
                    e.getNombreEmpleadoServicio(),
                    e.getApellidoEmpleadoServicio(),
                    e.getTelefonoEmpleadoServicio()
            );
        }
    }
}