package RokerPeru.demo.services;

import RokerPeru.demo.dto.DetallePedidoDTO;
import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.entities.DetallePedido;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.exception.StockInsuficienteException;
import RokerPeru.demo.repository.PedidosRepository;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidosRepository pedidosRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ComprobanteService comprobanteService;

    // Crear un nuevo pedido del cliente
    @Transactional
    public PedidoDTO.ResponseCliente crearPedido(PedidoDTO.RequestCliente dto, Long idCliente) {
        Usuario cliente = obtenerUsuario(idCliente);
        // Delegar la creación de la entidad base al DTO
        Pedidos pedido = dto.toEntity(cliente);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("PENDIENTE");


        double montoTotal = 0.0;

        // Procesar cada detalle del pedido
        for (DetallePedidoDTO.Request detalleDto : dto.detalles()) {
            Producto producto = productoRepository.findById(detalleDto.idProducto())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", detalleDto.idProducto()));

            if (producto.getStockProducto() < detalleDto.cantidadProducto()) {
                throw new StockInsuficienteException(
                        producto.getNombreProducto(),
                        producto.getStockProducto(),
                        detalleDto.cantidadProducto()
                );
            }

            DetallePedido detalle = detalleDto.toEntity(pedido, producto, producto.getPrecioProducto());
            pedido.agregarDetalle(detalle);

            producto.setStockProducto(producto.getStockProducto() - detalleDto.cantidadProducto());
            montoTotal += detalle.getSubtotal();
        }

        pedido.setMontoTotalPedido(montoTotal);

        // Guardar y retornar
        Pedidos pedidoGuardado = pedidosRepository.save(pedido);
        log.info("Cliente {} creó el pedido #{} (pendiente de confirmación). Total: S/ {}",
                cliente.getNombreUsuario(), pedidoGuardado.getIdPedido(), montoTotal);

        return PedidoDTO.ResponseCliente.fromEntity(pedidoGuardado);
    }

    // Confirmar pedido, asignar repartidor y completar datos para el vendedor
    @Transactional
    public PedidoDTO.ResponseVendedor confirmarYAsignarPedido(Long idPedido, PedidoDTO.RequestAsignacionVendedor dto, Long idVendedorLogueado) {
        // Obtener el pedido
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", idPedido));

        // Validar regla de negocio: solo se pueden procesar pedidos pendientes
        if (!"PENDIENTE".equals(pedido.getEstadoPedido())) {
            throw new OperacionNoPermitidaException("Solo se pueden procesar pedidos que estén en estado PENDIENTE");
        }

        // Obtener las entidades de usuario necesarias
        Usuario vendedor = obtenerUsuario(idVendedorLogueado);
        Usuario delivery = obtenerUsuario(dto.idDelivery());

        // Validar que el usuario asignado tenga rol de repartidor
        if (delivery.getRol() == null || !"REPARTIDOR".equalsIgnoreCase(delivery.getRol().getNombreRol())) {
            throw new ReglaNegocioException("El usuario seleccionado no tiene el rol de REPARTIDOR");
        }

        // El vendedor se asigna a sí mismo al pedido
        pedido.setVendedor(vendedor);

        // Delegar la actualización de campos de gestión al DTO
        dto.applyTo(pedido, delivery);
        // Si el vendedor no indicó la salida, sale ahora
        if (pedido.getFechaSalidaPedido() == null) {
            pedido.setFechaSalidaPedido(LocalDateTime.now());
        }

        // La entrega no puede ser antes de la salida
        if (pedido.getFechaEntregaPedido() != null
                && pedido.getFechaEntregaPedido().isBefore(pedido.getFechaSalidaPedido())) {
            throw new ReglaNegocioException("La fecha de entrega no puede ser anterior a la fecha de salida");
        }        // Actualizar el estado a "EN_RUTA" (o "CONFIRMADO", según tu flujo de negocio)
        pedido.setEstadoPedido("EN_RUTA");
        emitirComprobante(pedido);
        // Guardar y retornar la vista COMPLETA para el vendedor
        Pedidos pedidoActualizado = pedidosRepository.save(pedido);
        log.info("Vendedor {} confirmó el pedido #{} y lo asignó al repartidor {}", vendedor.getNombreUsuario(), idPedido, delivery.getNombreUsuario());

        return PedidoDTO.ResponseVendedor.fromEntity(pedidoActualizado);
    }

    // Actualizar estado (Entregado/Cancelado) e incidentes para el repartidor
    @Transactional
    public PedidoDTO.ResponseRepartidor actualizarEstadoRepartidor(Long idPedido, Long idRepartidorLogueado, PedidoDTO.RequestActualizacionRepartidor dto) {
        // Validar que el pedido esté asignado a ESTE repartidor
        boolean esSuPedido = pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(idPedido, idRepartidorLogueado);
        if (!esSuPedido) {
            throw new OperacionNoPermitidaException("Acceso denegado: Este pedido no está asignado a ti");
        }

        // Obtener el pedido
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", idPedido));

        // Delegar la actualización de estado e incidentes al DTO
        dto.applyTo(pedido);

        // Regla de negocio: Si se marca como ENTREGADO, registrar la fecha y hora real
        if ("ENTREGADO".equals(dto.nuevoEstado())) {
            pedido.setFechaEntregaPedido(LocalDateTime.now());
        }

        // Guardar y retornar la vista limitada para el repartidor
        Pedidos pedidoActualizado = pedidosRepository.save(pedido);
        log.info("Repartidor actualizó el pedido #{} al estado: {}", idPedido, dto.nuevoEstado());

        return PedidoDTO.ResponseRepartidor.fromEntity(pedidoActualizado);
    }

    // Consultas segun rol
    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseCliente> listarMisPedidos(Long idCliente) {
        return pedidosRepository.findByCliente_IdUsuarioOrderByFechaPedidoDesc(idCliente)
                .stream()
                .map(PedidoDTO.ResponseCliente::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseVendedor> listarPedidosParaGestionar() {
        // El vendedor ve todos los pedidos pendientes para procesarlos
        return pedidosRepository.findByEstadoPedidoOrderByFechaPedidoDesc("PENDIENTE")
                .stream()
                .map(PedidoDTO.ResponseVendedor::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseVendedor> listarVentasDelVendedor(Long idVendedor) {
        return pedidosRepository.findByVendedor_IdUsuarioOrderByFechaPedidoDesc(idVendedor)
                .stream()
                .map(PedidoDTO.ResponseVendedor::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseRepartidor> listarPedidosAsignados(Long idRepartidor) {
        // El repartidor solo ve los pedidos que están en ruta o pendientes de entrega y que son suyos
        List<String> estadosActivos = List.of("EN_RUTA", "PENDIENTE_ENTREGA");
        return pedidosRepository.findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(idRepartidor, estadosActivos)
                .stream()
                .map(PedidoDTO.ResponseRepartidor::fromEntity)
                .toList();
    }

    // Métodos privados
    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de buscar usuario con ID inexistente: {}", id);
                    return new RecursoNoEncontradoException("Usuario", id);
                });
    }

    // Asigna serie + número. Si ya tiene número no hace nada (nunca se emite dos veces)
    private void emitirComprobante(Pedidos pedido) {
        if (pedido.getNumeroComprobantePedido() != null) {
            return;
        }
        TipoComprobanteEnum tipo = ComprobanteService.resolverTipo(pedido.getCliente(), false);
        ComprobanteService.NumeroComprobante numero = comprobanteService.emitir(tipo);

        pedido.setSerieComprobantePedido(numero.serie());
        pedido.setNumeroComprobantePedido(numero.numero());
        pedido.setFechaEmisionComprobante(LocalDateTime.now());
    }
}