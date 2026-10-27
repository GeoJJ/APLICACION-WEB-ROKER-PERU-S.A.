package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponseDTO {
    private Long idProveedor;
    private String nombreProveedor;
    private String rucProveedor;
    private String telefonoProveedor;
    private String correoProveedor;
}