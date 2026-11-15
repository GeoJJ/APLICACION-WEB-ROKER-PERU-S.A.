package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Proveedores;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ProveedorDTO {
    private ProveedorDTO() {}

    public record Request(
            @NotBlank(message = "El nombre del proveedor es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
            String nombreProveedor,

            @NotBlank(message = "El RUC es obligatorio")
            @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
            String rucProveedor,

            @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
            String telefonoProveedor,

            @Email(message = "El correo no tiene un formato válido")
            @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
            String correoProveedor
    ) {
        public Request {
            nombreProveedor = nombreProveedor != null ? nombreProveedor.trim().toUpperCase() : null;
            rucProveedor = rucProveedor != null ? rucProveedor.trim() : null;
            telefonoProveedor = (telefonoProveedor == null || telefonoProveedor.isBlank()) ? null : telefonoProveedor.trim();
            correoProveedor = (correoProveedor == null || correoProveedor.isBlank()) ? null : correoProveedor.trim().toLowerCase();
        }

        public Proveedores toEntity() {
            Proveedores proveedor = new Proveedores();
            applyTo(proveedor);
            return proveedor;
        }

        public void applyTo(Proveedores proveedor) {
            proveedor.setNombreProveedor(this.nombreProveedor);
            proveedor.setRucProveedor(this.rucProveedor);
            proveedor.setTelefonoProveedor(this.telefonoProveedor);
            proveedor.setCorreoProveedor(this.correoProveedor);
        }
    }

    public record Response(
            Long idProveedor,
            String nombreProveedor,
            String rucProveedor,
            String telefonoProveedor,
            String correoProveedor
    ) {
        public static Response fromEntity(Proveedores p) {
            return new Response(
                    p.getIdProveedor(),
                    p.getNombreProveedor(),
                    p.getRucProveedor(),
                    p.getTelefonoProveedor(),
                    p.getCorreoProveedor()
            );
        }
    }
}