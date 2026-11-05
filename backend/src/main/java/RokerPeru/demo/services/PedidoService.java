package RokerPeru.demo.services;

import RokerPeru.demo.dto.DetallePedidoDTO;
import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.entities.DetallePedido;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
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
    public PedidoDTO.ResponseCliente crearPedido(PedidoDTO.RequestCliente dto) {
        if(dto.detalles()==null||dto.detalles().isEmpty())throw new IllegalArgumentException("El pedido debe contener productos");
        // Obtener al cliente
        Usuario cliente = obtenerUsuario(dto.idCliente());

        // Delegar la creación de la entidad base al DTO
        Pedidos pedido = dto.toEntity(cliente);
        pedido.setFechaPedido(LocalDateTime.now());
        pedido.setEstadoPedido("PENDIENTE");

        TipoComprobanteEnum tipoComprobante;

        // Si es empresa (JURIDICA), le damos Factura. Si es persona (NATURAL), Boleta.
        if ("JURIDICA".equals(cliente.getTipoPersonaUsuario())) {
            tipoComprobante = TipoComprobanteEnum.FACTURA_PRODUCTO;
        } else {
            tipoComprobante = TipoComprobanteEnum.BOLETA_PRODUCTO;
        }
        String numeroGenerado = comprobanteService.generarNumeroComprobante(tipoComprobante);

        // Asignamos a la entidad
        pedido.setSerieComprobantePedido(tipoComprobante.getSerie());
        pedido.setNumeroComprobantePedido(numeroGenerado);
        // =========================================================================

        double montoTotal = 0.0;

        // Procesar cada detalle del pedido
        for (DetallePedidoDTO.Request detalleDto : dto.detalles()) {
            if(detalleDto.cantidadProducto()==null||detalleDto.cantidadProducto()<1)throw new IllegalArgumentException("Cantidad no válida");
            Producto producto = productoRepository.buscarParaActualizarStock(detalleDto.idProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDto.idProducto()));

            if("INACTIVO".equalsIgnoreCase(producto.getEstadoProducto()))throw new IllegalArgumentException("Producto inactivo");
            if (producto.getStockProducto() < detalleDto.cantidadProducto()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombreProducto());
            }

            DetallePedido detalle = detalleDto.toEntity(pedido, producto, producto.getPrecioProducto());
            pedido.agregarDetalle(detalle);

            producto.setStockProducto(producto.getStockProducto() - detalleDto.cantidadProducto());
            montoTotal += detalle.getSubtotal();
        }

        pedido.setMontoTotalPedido(montoTotal);

        // Guardar y retornar
        Pedidos pedidoGuardado = pedidosRepository.save(pedido);
        log.info("Cliente {} creó el pedido #{} con comprobante {}. Total: S/ {}",
                cliente.getNombreUsuario(), pedidoGuardado.getIdPedido(), numeroGenerado, montoTotal);

        return PedidoDTO.ResponseCliente.fromEntity(pedidoGuardado);
    }

    // Confirmar pedido, asignar repartidor y completar datos para el vendedor
    @Transactional
    public PedidoDTO.ResponseVendedor confirmarYAsignarPedido(Long idPedido, PedidoDTO.RequestAsignacionVendedor dto, Long idVendedorLogueado) {
        // Obtener el pedido
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + idPedido));

        // Validar regla de negocio: solo se pueden procesar pedidos pendientes
        if (!"PENDIENTE".equals(pedido.getEstadoPedido())) {
            throw new RuntimeException("Solo se pueden procesar pedidos que estén en estado PENDIENTE");
        }

        // Obtener las entidades de usuario necesarias
        Usuario vendedor = obtenerUsuario(idVendedorLogueado);
        Usuario delivery = obtenerUsuario(dto.idDelivery());

        // Validar que el usuario asignado tenga rol de repartidor
        if (!delivery.isActivo() || delivery.getRol() == null || !"REPARTIDOR".equalsIgnoreCase(delivery.getRol().getNombreRol())) {
            throw new RuntimeException("El usuario seleccionado no tiene el rol de REPARTIDOR");
        }

        // El vendedor se asigna a sí mismo al pedido
        pedido.setVendedor(vendedor);

        // Delegar la actualización de campos de gestión al DTO
        dto.applyTo(pedido, delivery);

        // Actualizar el estado a "EN_RUTA" (o "CONFIRMADO", según tu flujo de negocio)
        pedido.setEstadoPedido("EN_RUTA");

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
            throw new RuntimeException("Acceso denegado: Este pedido no está asignado a ti");
        }

        // Obtener el pedido
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + idPedido));

        if(!List.of("EN_RUTA","ENTREGADO").contains(dto.nuevoEstado())||List.of("ENTREGADO","CANCELADO").contains(pedido.getEstadoPedido()))throw new IllegalArgumentException("Transición de entrega no válida");
        if("ENTREGADO".equals(dto.nuevoEstado())&&(pedido.getEvidencia()==null||pedido.getFirma()==null))throw new IllegalArgumentException("Adjunta la evidencia y la firma desde el panel de entregas");
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
    public List<PedidoDTO.ResponseRepartidor> listarPedidosAsignados(Long idRepartidor) {
        // El repartidor solo ve los pedidos que están en ruta o pendientes de entrega y que son suyos
        List<String> estadosActivos = List.of("EN_RUTA", "PENDIENTE_ENTREGA");
        return pedidosRepository.findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(idRepartidor, estadosActivos)
                .stream()
                .map(PedidoDTO.ResponseRepartidor::fromEntity)
                .toList();
    }

    // Métodos privados
    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseVendedor> listarTodos() { return pedidosRepository.findAll().stream().map(PedidoDTO.ResponseVendedor::fromEntity).toList(); }
    @Transactional(readOnly = true)
    public PedidoDTO.ResponseVendedor buscarVendedor(Long id) { return PedidoDTO.ResponseVendedor.fromEntity(pedidosRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"))); }
    @Transactional(readOnly = true)
    public PedidoDTO.ResponseCliente buscarCliente(Long id, Long user) {
        var p = pedidosRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
        if (!p.getCliente().getIdUsuario().equals(user)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        return PedidoDTO.ResponseCliente.fromEntity(p);
    }
    @Transactional
    public PedidoDTO.ResponseCliente cancelarCliente(Long id, Long user) {
        buscarCliente(id,user); var p=pedidosRepository.findById(id).orElseThrow();
        if (!"PENDIENTE".equals(p.getEstadoPedido())) throw new IllegalArgumentException("Solo puedes cancelar pedidos pendientes");
        p.getDetallePedidos().forEach(d -> { var product=productoRepository.buscarParaActualizarStock(d.getProducto().getIdProducto()).orElseThrow(); product.setStockProducto(product.getStockProducto()+d.getCantidadProducto()); });
        p.setEstadoPedido("CANCELADO"); return PedidoDTO.ResponseCliente.fromEntity(p);
    }
    @Transactional(readOnly = true)
    public List<PedidoDTO.ResponseRepartidor> listarAsignadosHistorial(Long user) { return pedidosRepository.findAll().stream().filter(p -> p.getDelivery()!=null && p.getDelivery().getIdUsuario().equals(user)).map(PedidoDTO.ResponseRepartidor::fromEntity).toList(); }
    @Transactional(readOnly = true)
    public PedidoDTO.ResponseRepartidor buscarRepartidor(Long id,Long user) { return listarAsignadosHistorial(user).stream().filter(p -> p.idPedido().equals(id)).findFirst().orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)); }
    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de buscar usuario con ID inexistente: {}", id);
                    return new RuntimeException("Usuario no encontrado con ID: " + id);
                });
    }
}
