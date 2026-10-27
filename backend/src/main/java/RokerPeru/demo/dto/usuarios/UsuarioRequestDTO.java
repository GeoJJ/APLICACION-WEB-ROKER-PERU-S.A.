package RokerPeru.demo.dto.usuarios;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
    private String correoUsuario;

    @NotBlank(message = "El tipo de persona es obligatorio")
    @Pattern(regexp = "NATURAL|JURIDICA", message = "El tipo de persona debe ser NATURAL o JURIDICA")
    private String tipoPersonaUsuario;

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "\\d{8}|\\d{11}", message = "El documento debe tener 8 dígitos (DNI) u 11 dígitos (RUC)")
    private String numeroDocumentoUsuario;

    @Pattern(regexp = "^(\\d{9})?$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoUsuario;      // opcional

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    private String direccionUsuario;     // opcional

    @NotNull(message = "El rol es obligatorio")
    private Long idRol;

    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String passwordUsuario;
}