package RokerPeru.demo.services;

import RokerPeru.demo.dto.pedido.PedidoRequestDTO;
import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import RokerPeru.demo.dto.detallePedidos.DetallePedidoRequestDTO;
import RokerPeru.demo.dto.detallePedidos.DetallePedidoResponseDTO;
import RokerPeru.demo.entities.*;
import RokerPeru.demo.repository.PedidosRepository;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidosRepository pedidosRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    
    @Transactional
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
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombreProducto());
            }

            double precio = producto.getPrecioProducto();
            double subtotal = precio * d.getCantidadProducto();

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidadProducto(d.getCantidadProducto());
            detalle.setPrecioUnitarioProducto(precio);
            detalle.setSubtotal(subtotal);
            pedido.getDetallePedidos().add(detalle);

            // Descontar stock 
            producto.setStockProducto(producto.getStockProducto() - d.getCantidadProducto());
            total += subtotal;
        }

        pedido.setMontoTotalPedido(total);
        return aDto(pedidosRepository.save(pedido));
    }

    
    @Transactional
    public PedidoResponseDTO confirmarPedido(Long idPedido) {
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));
        pedido.setEstadoPedido("CONFIRMADO");
        return aDto(pedidosRepository.save(pedido));
    }

    
    @Transactional
    public PedidoResponseDTO cancelarPedido(Long idPedido) {
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));
        
        // Devolver el stock al inventario
        for (DetallePedido detalle : pedido.getDetallePedidos()) {
            Producto producto = detalle.getProducto();
            producto.setStockProducto(producto.getStockProducto() + detalle.getCantidadProducto());
            productoRepository.save(producto);
        }
        
        pedido.setEstadoPedido("CANCELADO");
        return aDto(pedidosRepository.save(pedido));
    }

    
    public List<PedidoResponseDTO> listarPedidosVendedor() {
        return pedidosRepository.findAll().stream().map(this::aDto).collect(Collectors.toList());
    }

    
    public List<PedidoResponseDTO> listarPedidosCliente(Long idCliente) {
        return pedidosRepository.findAll().stream()
                .filter(p -> p.getCliente().getIdUsuario().equals(idCliente))
                .map(this::aDto)
                .collect(Collectors.toList());
    }

    
    public List<PedidoResponseDTO> listarPedidoRepartidor() {
        return pedidosRepository.findAll().stream()
                .filter(p -> p.getEstadoPedido().equals("CONFIRMADO") || p.getEstadoPedido().equals("EN_RUTA"))
                .map(this::aDto)
                .collect(Collectors.toList());
    }

    
    public PedidoResponseDTO buscarPedidosVendedor(Long idPedido) {
        return aDto(pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido)));
    }

    
    public PedidoResponseDTO buscarPedidosCliente(Long idPedido, Long idCliente) {
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));
        
        if (!pedido.getCliente().getIdUsuario().equals(idCliente)) {
            throw new RuntimeException("Acceso denegado: El pedido no pertenece a este cliente");
        }
        return aDto(pedido);
    }

    
    public PedidoResponseDTO buscarPedidoRepartidor(Long idPedido) {
        return aDto(pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido)));
    }

    // Métodos privados auxiliares
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
                .collect(Collectors.toList());

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