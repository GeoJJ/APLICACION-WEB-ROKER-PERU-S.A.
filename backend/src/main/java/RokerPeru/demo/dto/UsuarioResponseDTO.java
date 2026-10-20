package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombreUsuario;
    private String correoUsuario;
    private String tipoPersonaUsuario;
    private String numeroDocumentoUsuario;
    private String telefonoUsuario;
    private String direccionUsuario;
    private String nombreRol;
}