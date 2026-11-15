package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import jakarta.validation.constraints.*;

public final class ProductoDTO {
    private ProductoDTO() {
    }

    public record Request(
            @NotBlank(message = "El código es obligatorio")
            @Size(max = 50, message = "El código no puede superar los 50 caracteres")
            String codigoProducto,

            @NotBlank(message = "El nombre del producto es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
            String nombreProducto,

            @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
            String categoriaProducto,

            @NotNull(message = "El precio es obligatorio")
            @PositiveOrZero(message = "El precio no puede ser negativo")
            Double precioProducto,

            // Stock INICIAL: solo se usa al crear. Después cambia únicamente desde InventarioService.
            @NotNull(message = "El stock es obligatorio")
            @PositiveOrZero(message = "El stock no puede ser negativo")
            Integer stockProducto,

            // Opcional: si no se envía, al crear queda en 10 y al actualizar no se modifica
            @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
            Integer stockMinimo,

            @Size(max = 30, message = "El estado no puede superar los 30 caracteres")
            String estadoProducto,

            @NotNull(message = "El proveedor es obligatorio")
            Long idProveedor
    ) {
        public Request {
            codigoProducto = codigoProducto != null ? codigoProducto.trim().toUpperCase() : null;
            nombreProducto = nombreProducto != null ? nombreProducto.trim() : null;
            categoriaProducto = categoriaProducto != null ? categoriaProducto.trim().toUpperCase() : null;
            estadoProducto = estadoProducto != null ? estadoProducto.trim().toUpperCase() : null;
        }

        public Producto toEntity(Proveedores proveedor) {
            Producto producto = new Producto();
            applyTo(producto, proveedor);
            producto.setStockProducto(this.stockProducto);   // stock inicial: solo al crear
            return producto;
        }

        public void applyTo(Producto producto, Proveedores proveedor) {
            producto.setCodigoProducto(this.codigoProducto);
            producto.setNombreProducto(this.nombreProducto);
            producto.setCategoriaProducto(this.categoriaProducto);
            producto.setPrecioProducto(this.precioProducto);
            producto.setEstadoProducto(this.estadoProducto != null ? this.estadoProducto : "ACTIVO");
            producto.setProveedor(proveedor);
            if (this.stockMinimo != null) {
                producto.setStockMinimo(this.stockMinimo);
            }
            // El stock NO se toca aquí: así Admin/Vendedor no pueden cambiarlo sin dejar movimiento
        }
    }

    // Vista Admin / Vendedor
    public record Response(
            Long idProducto,
            String codigoProducto,
            String nombreProducto,
            String categoriaProducto,
            Double precioProducto,
            Integer stockProducto,
            Integer stockMinimo,
            String estadoProducto,
            Long idProveedor,
            String nombreProveedor
    ) {
        public static Response fromEntity(Producto p) {
            return new Response(
                    p.getIdProducto(),
                    p.getCodigoProducto(),
                    p.getNombreProducto(),
                    p.getCategoriaProducto(),
                    p.getPrecioProducto(),
                    p.getStockProducto(),
                    p.getStockMinimo(),
                    p.getEstadoProducto(),
                    p.getProveedor() != null ? p.getProveedor().getIdProveedor() : null,
                    p.getProveedor() != null ? p.getProveedor().getNombreProveedor() : "Sin proveedor"
            );
        }
    }

    // Vista Cliente
    public record ClienteResponse(
            Long idProducto,
            String nombreProducto,
            String categoriaProducto,
            Double precioProducto,
            boolean disponible
    ) {
        public static ClienteResponse fromEntity(Producto p) {
            return new ClienteResponse(
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getCategoriaProducto(),
                    p.getPrecioProducto(),
                    p.getStockProducto() > 0
            );
        }
    }
}