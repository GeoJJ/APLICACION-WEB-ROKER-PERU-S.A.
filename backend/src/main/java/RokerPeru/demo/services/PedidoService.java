package RokerPeru.demo.services;

import RokerPeru.demo.dto.DetallePedidoRequestDTO;
import RokerPeru.demo.dto.DetallePedidoResponseDTO;
import RokerPeru.demo.dto.PedidoRequestDTO;
import RokerPeru.demo.dto.PedidoResponseDTO;
import RokerPeru.demo.entities.*;
import RokerPeru.demo.repository.PedidosRepository;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidosRepository pedidosRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoResponseDTO crearPedido(PedidoRequestDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos un producto");
        }

        Pedidos pedido = new Pedidos();
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("PENDIENTE");
        pedido.setSerieComprobantePedido(dto.getSerieComprobantePedido());
        pedido.setNumeroComprobantePedido(dto.getNumeroComprobantePedido());
        pedido.setMetodoPagoPedido(dto.getMetodoPagoPedido());

        pedido.setCliente(buscarUsuario(dto.getIdCliente()));
        if (dto.getIdVendedor() != null) pedido.setVendedor(buscarUsuario(dto.getIdVendedor()));
        if (dto.getIdDelivery() != null) pedido.setDelivery(buscarUsuario(dto.getIdDelivery()));
        if (dto.getIdAuxiliar() != null) pedido.setAuxiliar(buscarUsuario(dto.getIdAuxiliar()));

        double total = 0;

        for (DetallePedidoRequestDTO d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + d.getIdProducto()));

            if (d.getCantidadProducto() == null || d.getCantidadProducto() <= 0) {
                throw new RuntimeException("Cantidad inválida para el producto: " + producto.getNombreProducto());
            }
            if (producto.getStockProducto() < d.getCantidadProducto()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombreProducto()
                        + " (disponible: " + producto.getStockProducto() + ")");
            }

            // El precio sale de la BD, no del cliente
            double precio = producto.getPrecioProducto();
            double subtotal = precio * d.getCantidadProducto();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidadProducto(d.getCantidadProducto());
            detalle.setPrecioUnitarioProducto(precio);
            detalle.setSubtotal(subtotal);
            pedido.getDetallePedidos().add(detalle);

            // Descontar stock (Hibernate lo guarda solo al cerrar la transacción)
            producto.setStockProducto(producto.getStockProducto() - d.getCantidadProducto());

            total += subtotal;
        }

        pedido.setMontoTotalPedido(total);

        return aDto(pedidosRepository.save(pedido));
    }

    public List<PedidoResponseDTO> listarPedido() {
        return pedidosRepository.findAll().stream().map(this::aDto).toList();
    }

    public PedidoResponseDTO buscarPorIdPedido(Long id) {
        return aDto(pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id)));
    }

    public PedidoResponseDTO cambiarEstadoPedido(Long id, String nuevoEstado) {
        Pedidos pedido = pedidosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
        pedido.setEstadoPedido(nuevoEstado);
        return aDto(pedido);
    }

    private Usuario buscarUsuario(Long id) {
        if (id == null) throw new RuntimeException("Falta el id del usuario");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    private PedidoResponseDTO aDto(Pedidos p) {
        List<DetallePedidoResponseDTO> detalles = p.getDetallePedidos().stream()
                .map(d -> new DetallePedidoResponseDTO(
                        d.getIdDetallePedido(),
                        d.getProducto().getIdProducto(),
                        d.getProducto().getNombreProducto(),
                        d.getCantidadProducto(),
                        d.getPrecioUnitarioProducto(),
                        d.getSubtotal()))
                .toList();

        return new PedidoResponseDTO(
                p.getIdPedido(),
                p.getFechaPedido(),
                p.getSerieComprobantePedido(),
                p.getNumeroComprobantePedido(),
                p.getEstadoPedido(),
                p.getMontoTotalPedido(),
                p.getMetodoPagoPedido(),
                p.getCliente().getNombreUsuario(),
                p.getVendedor() != null ? p.getVendedor().getNombreUsuario() : null,
                detalles
        );
    }
}