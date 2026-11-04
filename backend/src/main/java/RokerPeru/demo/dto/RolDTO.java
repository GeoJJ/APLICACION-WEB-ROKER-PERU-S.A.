package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class RolDTO {
    private RolDTO() {
    }

    public record Request(
            @NotBlank(message = "El nombre del rol es obligatorio")
            @Size(min = 3, max = 50, message = "El nombre del rol debe tener entre 3 y 50 caracteres")
            String nombreRol) {
        public Request {
            nombreRol = nombreRol != null ? nombreRol.trim() : null;
        }

        public Rol toEntity(Rol rol) {
            rol.setNombreRol(this.nombreRol);
            return rol;
        }
    }

    public record Response(
            Long idRol,
            String nombreRol) {
        public static Response fromEntity(Rol rol) {
            return new Response(rol.getIdRol(), rol.getNombreRol());
        }
    }
}
