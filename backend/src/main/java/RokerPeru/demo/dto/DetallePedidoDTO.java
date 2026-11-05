package RokerPeru.demo.dto;

import RokerPeru.demo.entities.DetallePedido;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Producto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class DetallePedidoDTO {
    private DetallePedidoDTO() {}

    public record Request(
            @NotNull(message = "El ID del producto es obligatorio")
            Long idProducto,

            @NotNull(message = "La cantidad del producto es obligatoria")
            @Min(value = 1, message = "La cantidad del producto debe ser al menos 1")
            Integer cantidadProducto
    ) {
        // Crea un nuevo detalle asignándole el pedido, producto y precio calculado
        public DetallePedido toEntity(Pedidos pedido, Producto producto, double precioUnitario) {
            DetallePedido detalle = new DetallePedido();
            applyTo(detalle, producto, precioUnitario);
            detalle.setPedido(pedido); // El pedido se asigna aquí para mantener la relación
            return detalle;
        }

        // Actualiza o mapea los campos del detalle
        public void applyTo(DetallePedido detalle, Producto producto, double precioUnitario) {
            detalle.setProducto(producto);
            detalle.setCantidadProducto(this.cantidadProducto);
            detalle.setPrecioUnitarioProducto(precioUnitario);
            detalle.setSubtotal(precioUnitario * this.cantidadProducto);
        }
    }

    public record Response(
            Long idDetallePedido,
            Long idProducto,
            String nombreProducto,
            Integer cantidadProducto,
            double precioUnitarioProducto,
            double subtotal
    ) {
        public static Response fromEntity(DetallePedido d) {
            return new Response(
                    d.getIdDetallePedido(),
                    d.getProducto().getIdProducto(),
                    d.getProducto().getNombreProducto(),
                    d.getCantidadProducto(),
                    d.getPrecioUnitarioProducto(),
                    d.getSubtotal()
            );
        }
    }
}