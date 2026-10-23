package RokerPeru.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequestDTO {
    private String nombreProveedor;
    private String rucProveedor;
    private String telefonoProveedor;
    private String correoProveedor;
}