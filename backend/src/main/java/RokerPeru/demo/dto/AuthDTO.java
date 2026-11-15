package RokerPeru.demo.dto;

import jakarta.validation.constraints.NotBlank;

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

    public record Response(
            String token
    ) {
        // En un futuro, si queremos devolver también el rol o nombre del usuario al frontend 
        // junto con el token, simplemente se agrega aquí.
    }
}