package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public final class UsuarioDTO {
    private UsuarioDTO() {}

    public record RequestDTO(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
            String nombreUsuario,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
            String correoUsuario,

            @NotBlank(message = "El tipo de persona es obligatorio")
            @Pattern(regexp = "NATURAL|JURIDICA", message = "El tipo de persona debe ser NATURAL o JURIDICA")
            String tipoPersonaUsuario,

            @NotBlank(message = "El número de documento es obligatorio")
            String numeroDocumentoUsuario, // Quitamos el @Pattern de aquí para validar la regla cruzada abajo

            @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos")
            String telefonoUsuario,

            @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
            String direccionUsuario,

            @NotNull(message = "El rol es obligatorio")
            Long idRol,

            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String passwordUsuario
    ) {
        public RequestDTO {
            nombreUsuario = nombreUsuario != null ? nombreUsuario.trim() : null;
            correoUsuario = correoUsuario != null ? correoUsuario.trim().toLowerCase() : null;
            tipoPersonaUsuario = tipoPersonaUsuario != null ? tipoPersonaUsuario.trim().toUpperCase() : null;
            numeroDocumentoUsuario = numeroDocumentoUsuario != null ? numeroDocumentoUsuario.trim() : null;
            telefonoUsuario = (telefonoUsuario == null || telefonoUsuario.isBlank()) ? null : telefonoUsuario.trim();
            direccionUsuario = (direccionUsuario == null || direccionUsuario.isBlank()) ? null : direccionUsuario.trim();

            if (tipoPersonaUsuario != null && numeroDocumentoUsuario != null) {
                if (tipoPersonaUsuario.equals("NATURAL") && numeroDocumentoUsuario.length() != 8) {
                    throw new IllegalArgumentException("Una persona natural debe tener un DNI de 8 dígitos");
                }
                if (tipoPersonaUsuario.equals("JURIDICA") && numeroDocumentoUsuario.length() != 11) {
                    throw new IllegalArgumentException("Una persona jurídica debe tener un RUC de 11 dígitos");
                }
            }
        }

        public void toEntity(Usuario usuario, Rol rol) {
            usuario.setNombreUsuario(this.nombreUsuario);
            usuario.setCorreoUsuario(this.correoUsuario);
            usuario.setTipoPersonaUsuario(this.tipoPersonaUsuario);
            usuario.setNumeroDocumentoUsuario(this.numeroDocumentoUsuario);
            usuario.setTelefonoUsuario(this.telefonoUsuario);
            usuario.setDireccionUsuario(this.direccionUsuario);
            usuario.setRol(rol);
        }
    }

    public record ResponseDTO(
            Long idUsuario,
            String nombreUsuario,
            String correoUsuario,
            String tipoPersonaUsuario,
            String numeroDocumentoUsuario,
            String telefonoUsuario,
            String direccionUsuario,
            Long idRol,
            String nombreRol
    ) {
        public static ResponseDTO fromEntity(Usuario u) {
            return new ResponseDTO(
                    u.getIdUsuario(),
                    u.getNombreUsuario(),
                    u.getCorreoUsuario(),
                    u.getTipoPersonaUsuario(),
                    u.getNumeroDocumentoUsuario(),
                    u.getTelefonoUsuario(),
                    u.getDireccionUsuario(),
                    u.getRol().getIdRol(),
                    u.getRol().getNombreRol()
            );
        }
    }
}
