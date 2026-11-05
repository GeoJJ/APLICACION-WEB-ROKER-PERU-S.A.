package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public final class PedidoDTO {
    private PedidoDTO() {
    }
    // Solo para que el CLIENTE cree el pedido
    public record RequestCliente(
            @NotBlank(message = "El método de pago es obligatorio")
            String metodoPagoPedido,

            @NotNull(message = "El ID del cliente es obligatorio")
            Long idCliente,

            @NotNull(message = "El pedido debe contener al menos un detalle")
            List<DetallePedidoDTO.Request> detalles
    ) {
        public RequestCliente {
            metodoPagoPedido = metodoPagoPedido != null ? metodoPagoPedido.trim().toUpperCase() : null;
        }

        public Pedidos toEntity(Usuario cliente) {
            Pedidos pedido = new Pedidos();
            applyTo(pedido, cliente, null, null);
            return pedido;
        }

        public void applyTo(Pedidos pedido, Usuario cliente, Usuario vendedor, Usuario delivery) {
            pedido.setCliente(cliente);
            if (vendedor != null) pedido.setVendedor(vendedor);
            if (delivery != null) pedido.setDelivery(delivery);

            pedido.setMetodoPagoPedido(this.metodoPagoPedido);
        }
    }

    // Para que el VENDEDOR asigne y complete datos
    public record RequestAsignacionVendedor(
            @NotNull(message = "El ID del repartidor (delivery) es obligatorio")
            Long idDelivery,

            LocalDateTime fechaSalidaPedido,
            LocalDateTime fechaEntregaPedido
    ) {
        // Solo actualiza los campos que el vendedor tiene permiso de modificar
        public void applyTo(Pedidos pedido, Usuario delivery) {
            pedido.setDelivery(delivery);
            if (this.fechaSalidaPedido != null) {
                pedido.setFechaSalidaPedido(this.fechaSalidaPedido);
            }
            if (this.fechaEntregaPedido != null) {
                pedido.setFechaEntregaPedido(this.fechaEntregaPedido);
            }
        }
    }

    // Para que el REPARTIDOR actualice estado e incidentes
    public record RequestActualizacionRepartidor(
            @NotBlank(message = "El nuevo estado del pedido es obligatorio")
            String nuevoEstado,

            String incidentePedido
    ) {
        public RequestActualizacionRepartidor {
            nuevoEstado = nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : null;
            incidentePedido = incidentePedido != null ? incidentePedido.trim() : null;
        }

        // Solo actualiza estado e incidentes
        public void applyTo(Pedidos pedido) {
            pedido.setEstadoPedido(this.nuevoEstado);
            if (this.incidentePedido != null && !this.incidentePedido.isBlank()) {
                pedido.setIncidentePedido(this.incidentePedido);
            }
        }
    }

    // Lo que ve el CLIENTE
    public record ResponseCliente(
            Long idPedido,
            LocalDateTime fechaPedido,
            String serieComprobantePedido,
            String numeroComprobantePedido,
            LocalDateTime fechaSalidaPedido,
            LocalDateTime fechaEntregaPedido,
            String incidentePedido,
            String estadoPedido,
            double montoTotalPedido,
            String metodoPagoPedido,
            String nombreVendedor,
            String nombreDelivery,
            List<DetallePedidoDTO.Response> detalles
    ) {
        public static ResponseCliente fromEntity(Pedidos p) {
            return new ResponseCliente(
                    p.getIdPedido(),
                    p.getFechaPedido(),
                    p.getSerieComprobantePedido(),
                    p.getNumeroComprobantePedido(),
                    p.getFechaSalidaPedido(),
                    p.getFechaEntregaPedido(),
                    p.getIncidentePedido(),
                    p.getEstadoPedido(),
                    p.getMontoTotalPedido(),
                    p.getMetodoPagoPedido(),
                    p.getVendedor() != null ? p.getVendedor().getNombreUsuario() : "Por asignar",
                    p.getDelivery() != null ? p.getDelivery().getNombreUsuario() : "Por asignar",
                    p.getDetallePedidos().stream().map(DetallePedidoDTO.Response::fromEntity).toList()
            );
        }
    }

    // Lo que ve el VENDEDOR (TODO)
    public record ResponseVendedor(
            Long idPedido,
            LocalDateTime fechaPedido,
            String serieComprobantePedido,
            String numeroComprobantePedido,
            LocalDateTime fechaSalidaPedido,
            LocalDateTime fechaEntregaPedido,
            String incidentePedido,
            String estadoPedido,
            double montoTotalPedido,
            String metodoPagoPedido,
            Long idCliente, String nombreCliente, String direccionCliente,
            Long idVendedor, String nombreVendedor,
            Long idDelivery, String nombreDelivery,
            List<DetallePedidoDTO.Response> detalles
    ) {
        public static ResponseVendedor fromEntity(Pedidos p) {
            return new ResponseVendedor(
                    p.getIdPedido(),
                    p.getFechaPedido(),
                    p.getSerieComprobantePedido(),
                    p.getNumeroComprobantePedido(),
                    p.getFechaSalidaPedido(),
                    p.getFechaEntregaPedido(),
                    p.getIncidentePedido(),
                    p.getEstadoPedido(),
                    p.getMontoTotalPedido(),
                    p.getMetodoPagoPedido(),
                    p.getCliente() != null ? p.getCliente().getIdUsuario() : null,
                    p.getCliente() != null ? p.getCliente().getNombreUsuario() : null,
                    p.getCliente() != null ? p.getCliente().getDireccionUsuario() : null,
                    p.getVendedor() != null ? p.getVendedor().getIdUsuario() : null,
                    p.getVendedor() != null ? p.getVendedor().getNombreUsuario() : null,
                    p.getDelivery() != null ? p.getDelivery().getIdUsuario() : null,
                    p.getDelivery() != null ? p.getDelivery().getNombreUsuario() : null,
                    p.getDetallePedidos().stream().map(DetallePedidoDTO.Response::fromEntity).toList()
            );
        }
    }

    // Lo que ve el REPARTIDOR (Solo lo esencial)
    public record ResponseRepartidor(
            Long idPedido,
            String estadoPedido,
            String nombreCliente,
            String direccionEntregaCliente,
            String telefonoCliente,
            String incidentePedido,
            LocalDateTime fechaEntregaPedido
    ) {
        public static ResponseRepartidor fromEntity(Pedidos p) {
            return new ResponseRepartidor(
                    p.getIdPedido(),
                    p.getEstadoPedido(),
                    p.getCliente() != null ? p.getCliente().getNombreUsuario() : "Desconocido",
                    p.getDireccionEntrega()!=null?p.getDireccionEntrega():p.getCliente() != null ? p.getCliente().getDireccionUsuario() : "Sin dirección",
                    p.getTelefonoEntrega()!=null?p.getTelefonoEntrega():p.getCliente() != null ? p.getCliente().getTelefonoUsuario() : "Sin teléfono",
                    p.getIncidentePedido(),
                    p.getFechaEntregaPedido()
            );
        }
    }
}