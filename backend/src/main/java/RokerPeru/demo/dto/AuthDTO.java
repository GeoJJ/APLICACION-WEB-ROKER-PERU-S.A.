package RokerPeru.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDTO {

    // Constructor privado para evitar que alguien instancie la clase contenedora
    private AuthDTO() {
    }

    public record LoginRequest(
            @NotBlank(message = "El correo no puede estar vacío")
            String correoUsuario,

            @NotBlank(message = "La contraseña no puede estar vacía")
            String passwordUsuario
    ) {
        // Bloque compacto de normalización de datos al recibir la petición
        public LoginRequest {
            correoUsuario = correoUsuario != null ? correoUsuario.trim().toLowerCase() : null;
        }
    }

    // Registro público de clientes. OJO: no trae idRol, el rol lo fija el backend (CLIENTE).
    public record RegisterRequest(
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
            @Pattern(regexp = "^(\\d{8}|\\d{11})$", message = "El documento debe ser un DNI de 8 dígitos o un RUC de 11")
            String numeroDocumentoUsuario,

            @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos")
            String telefonoUsuario,

            @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
            String direccionUsuario,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String passwordUsuario
    ) {
        public RegisterRequest {
            nombreUsuario = nombreUsuario != null ? nombreUsuario.trim() : null;
            correoUsuario = correoUsuario != null ? correoUsuario.trim().toLowerCase() : null;
            tipoPersonaUsuario = tipoPersonaUsuario != null ? tipoPersonaUsuario.trim().toUpperCase() : null;
            numeroDocumentoUsuario = numeroDocumentoUsuario != null ? numeroDocumentoUsuario.trim() : null;
            telefonoUsuario = (telefonoUsuario == null || telefonoUsuario.isBlank()) ? null : telefonoUsuario.trim();
            direccionUsuario = (direccionUsuario == null || direccionUsuario.isBlank()) ? null : direccionUsuario.trim();
        }
    }

    public record Response(
            String token
    ) {
        // En un futuro, si queremos devolver también el rol o nombre del usuario al frontend 
        // junto con el token, simplemente se agrega aquí.
    }
}