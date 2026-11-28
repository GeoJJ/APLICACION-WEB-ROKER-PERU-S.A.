package RokerPeru.demo.services;

import RokerPeru.demo.dto.DetallePedidoDTO;

import RokerPeru.demo.dto.PedidoDTO;

import RokerPeru.demo.entities.DetallePedido;

import RokerPeru.demo.entities.Pedidos;

import RokerPeru.demo.entities.Producto;

import RokerPeru.demo.entities.Rol;

import RokerPeru.demo.entities.Usuario;

import RokerPeru.demo.enums.TipoComprobanteEnum;

import RokerPeru.demo.exception.OperacionNoPermitidaException;

import RokerPeru.demo.exception.RecursoNoEncontradoException;

import RokerPeru.demo.exception.ReglaNegocioException;

import RokerPeru.demo.exception.StockInsuficienteException;

import RokerPeru.demo.repository.PedidosRepository;

import RokerPeru.demo.repository.ProductoRepository;

import RokerPeru.demo.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.CsvSource;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyDouble;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class PedidoServiceTest {

    @Mock

    private PedidosRepository pedidosRepository;

    @Mock

    private UsuarioRepository usuarioRepository;

    @Mock

    private ProductoRepository productoRepository;

    @Mock

    private ComprobanteService comprobanteService;

    @Mock
    private CodigoService codigoService;

    @InjectMocks

    private PedidoService pedidoService;

    // Mock del servicio de correo para que el test no envíe correos reales
    @Mock
    private EmailService emailService;

    // Mock del generador de PDF para que el test no genere PDFs reales
    @Mock
    private ComprobantePdfService comprobantePdfService;

    private Pedidos pedidoMock() {

        return mock(Pedidos.class, RETURNS_DEEP_STUBS);

    }

    // ---------- crearPedido ----------

    @ParameterizedTest

    @CsvSource({

            "JURIDICA,FACTURA_PRODUCTO",

            "NATURAL,BOLETA_PRODUCTO"

    })

    void crearPedido_segunTipoDePersona_reservaStockSinEmitirComprobante(

            String tipoPersona, TipoComprobanteEnum tipoEsperado) {

        PedidoDTO.RequestCliente dto = mock(PedidoDTO.RequestCliente.class);

        DetallePedidoDTO.Request detalleDto = mock(DetallePedidoDTO.Request.class);

        Usuario cliente = mock(Usuario.class);

        Producto producto = mock(Producto.class);

        DetallePedido detalle = mock(DetallePedido.class);

        Pedidos pedido = pedidoMock();

        // El ID del cliente se recibe desde la autenticación.

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));



        when(dto.toEntity(cliente)).thenReturn(pedido);

        when(dto.detalles()).thenReturn(List.of(detalleDto));

        when(detalleDto.idProducto()).thenReturn(5L);

        when(detalleDto.cantidadProducto()).thenReturn(3);

        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        when(producto.getStockProducto()).thenReturn(10);

        when(producto.getPrecioProducto()).thenReturn(10.0);

        when(detalleDto.toEntity(eq(pedido), eq(producto), anyDouble())).thenReturn(detalle);

        when(detalle.getSubtotal()).thenReturn(30.0);

        when(pedidosRepository.save(pedido)).thenReturn(pedido);

        PedidoDTO.ResponseCliente response = pedidoService.crearPedido(dto, 1L);

        assertThat(response).isNotNull();

        verify(pedido).setEstadoPedido("PENDIENTE");

        verify(pedido, never()).setSerieComprobantePedido(any());

        verifyNoInteractions(comprobanteService);

        verify(pedido).agregarDetalle(detalle);

        verify(producto).setStockProducto(7);

        verify(pedido).setMontoTotalPedido(30.0);

        verify(pedidosRepository).save(pedido);

    }

    @Test

    void crearPedido_conStockInsuficiente_lanzaExcepcionYNoGuarda() {

        PedidoDTO.RequestCliente dto = mock(PedidoDTO.RequestCliente.class);

        DetallePedidoDTO.Request detalleDto = mock(DetallePedidoDTO.Request.class);

        Usuario cliente = mock(Usuario.class);

        Producto producto = mock(Producto.class);

        // El ID del cliente se recibe desde la autenticación.

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(dto.toEntity(cliente)).thenReturn(pedidoMock());

        when(dto.detalles()).thenReturn(List.of(detalleDto));

        when(detalleDto.idProducto()).thenReturn(5L);

        when(detalleDto.cantidadProducto()).thenReturn(5);

        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        when(producto.getStockProducto()).thenReturn(2);

        when(producto.getNombreProducto()).thenReturn("Casco");

        assertThatThrownBy(() -> pedidoService.crearPedido(dto, 1L))

                .isInstanceOf(StockInsuficienteException.class);

        verify(producto, never()).setStockProducto(anyInt());

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void crearPedido_conProductoInexistente_lanzaRecursoNoEncontrado() {

        PedidoDTO.RequestCliente dto = mock(PedidoDTO.RequestCliente.class);

        DetallePedidoDTO.Request detalleDto = mock(DetallePedidoDTO.Request.class);

        Usuario cliente = mock(Usuario.class);

        // El ID del cliente se recibe desde la autenticación.

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(dto.toEntity(cliente)).thenReturn(pedidoMock());

        when(dto.detalles()).thenReturn(List.of(detalleDto));

        when(detalleDto.idProducto()).thenReturn(99L);

        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.crearPedido(dto, 1L))

                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void crearPedido_conClienteInexistente_lanzaExcepcion() {

        PedidoDTO.RequestCliente dto = mock(PedidoDTO.RequestCliente.class);

        // El ID del cliente se recibe desde la autenticación.

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.crearPedido(dto, 99L))

                .isInstanceOf(RuntimeException.class)

                .hasMessageContaining("99");

        verifyNoInteractions(productoRepository, comprobanteService);

        verify(pedidosRepository, never()).save(any());

    }

    // ---------- confirmarYAsignarPedido ----------

    @Test

    void confirmarYAsignarPedido_conRepartidorValido_poneEnRuta() {

        PedidoDTO.RequestAsignacionVendedor dto = mock(PedidoDTO.RequestAsignacionVendedor.class);

        Pedidos pedido = pedidoMock();

        Usuario vendedor = mock(Usuario.class);

        Usuario delivery = mock(Usuario.class);

        Rol rol = mock(Rol.class);

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));

        when(pedido.getEstadoPedido()).thenReturn("PENDIENTE");

        when(dto.idDelivery()).thenReturn(3L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vendedor));

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(delivery));

        when(delivery.getRol()).thenReturn(rol);

        when(rol.getNombreRol()).thenReturn("repartidor");

        when(pedidosRepository.save(pedido)).thenReturn(pedido);

        when(comprobanteService.emitir(any())).thenReturn(new ComprobanteService.NumeroComprobante("B001", "00000001"));
        PedidoDTO.ResponseVendedor response = pedidoService.confirmarYAsignarPedido(1L, dto, 2L);

        assertThat(response).isNotNull();

        verify(pedido).setVendedor(vendedor);

        verify(dto).applyTo(pedido, delivery);

        verify(pedido).setEstadoPedido("EN_RUTA");

        verify(pedidosRepository).save(pedido);

    }

    @Test

    void confirmarYAsignarPedido_cuandoPedidoNoExiste_lanzaExcepcion() {

        PedidoDTO.RequestAsignacionVendedor dto = mock(PedidoDTO.RequestAsignacionVendedor.class);

        when(pedidosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.confirmarYAsignarPedido(99L, dto, 2L))

                .isInstanceOf(RuntimeException.class)

                .hasMessageContaining("99");

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void confirmarYAsignarPedido_cuandoNoEstaPendiente_lanzaOperacionNoPermitida() {

        PedidoDTO.RequestAsignacionVendedor dto = mock(PedidoDTO.RequestAsignacionVendedor.class);

        Pedidos pedido = pedidoMock();

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));

        when(pedido.getEstadoPedido()).thenReturn("EN_RUTA");

        assertThatThrownBy(() -> pedidoService.confirmarYAsignarPedido(1L, dto, 2L))

                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void confirmarYAsignarPedido_conUsuarioSinRol_lanzaReglaNegocio() {

        PedidoDTO.RequestAsignacionVendedor dto = mock(PedidoDTO.RequestAsignacionVendedor.class);

        Pedidos pedido = pedidoMock();

        Usuario vendedor = mock(Usuario.class);

        Usuario delivery = mock(Usuario.class);

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));

        when(pedido.getEstadoPedido()).thenReturn("PENDIENTE");

        when(dto.idDelivery()).thenReturn(3L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vendedor));

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(delivery));

        when(delivery.getRol()).thenReturn(null);

        assertThatThrownBy(() -> pedidoService.confirmarYAsignarPedido(1L, dto, 2L))

                .isInstanceOf(ReglaNegocioException.class);

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void confirmarYAsignarPedido_conUsuarioQueNoEsRepartidor_lanzaReglaNegocio() {

        PedidoDTO.RequestAsignacionVendedor dto = mock(PedidoDTO.RequestAsignacionVendedor.class);

        Pedidos pedido = pedidoMock();

        Usuario vendedor = mock(Usuario.class);

        Usuario delivery = mock(Usuario.class);

        Rol rol = mock(Rol.class);

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));

        when(pedido.getEstadoPedido()).thenReturn("PENDIENTE");

        when(dto.idDelivery()).thenReturn(3L);

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vendedor));

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(delivery));

        when(delivery.getRol()).thenReturn(rol);

        when(rol.getNombreRol()).thenReturn("CLIENTE");

        assertThatThrownBy(() -> pedidoService.confirmarYAsignarPedido(1L, dto, 2L))

                .isInstanceOf(ReglaNegocioException.class)

                .hasMessageContaining("REPARTIDOR");

        verify(pedidosRepository, never()).save(any());

    }

    @Test
    void historialRepartidor_soloConsultaSusPedidosFinalizados() {
        when(pedidosRepository.findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(
                5L, List.of("ENTREGADO", "CANCELADO"))).thenReturn(List.of());
        assertThat(pedidoService.listarHistorialRepartidor(5L)).isEmpty();
        verify(pedidosRepository).findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(
                5L, List.of("ENTREGADO", "CANCELADO"));
    }

    @Test
    void actualizarEstadoRepartidor_siYaFueEntregado_noLoModifica() {
        Pedidos pedido = pedidoMock();
        when(pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(1L, 5L)).thenReturn(true);
        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedido.getEstadoPedido()).thenReturn("ENTREGADO");
        PedidoDTO.RequestActualizacionRepartidor dto = mock(PedidoDTO.RequestActualizacionRepartidor.class);
        assertThatThrownBy(() -> pedidoService.actualizarEstadoRepartidor(1L, 5L, dto))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("EN_RUTA");
        verify(dto, never()).applyTo(any());
        verify(pedidosRepository, never()).save(any());
    }

    // ---------- actualizarEstadoRepartidor ----------

    @Test

    void actualizarEstadoRepartidor_alMarcarEntregado_registraFechaDeEntrega() {

        PedidoDTO.RequestActualizacionRepartidor dto = mock(PedidoDTO.RequestActualizacionRepartidor.class);

        Pedidos pedido = pedidoMock();

        when(pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(1L, 5L)).thenReturn(true);

        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));

        when(pedido.getEstadoPedido()).thenReturn("EN_RUTA");
        when(dto.nuevoEstado()).thenReturn("ENTREGADO");

        when(pedidosRepository.save(pedido)).thenReturn(pedido);

        PedidoDTO.ResponseRepartidor response = pedidoService.actualizarEstadoRepartidor(1L, 5L, dto);

        assertThat(response).isNotNull();

        verify(dto).applyTo(pedido);

        verify(pedido).setFechaEntregaPedido(any(LocalDateTime.class));

        verify(pedidosRepository).save(pedido);

    }

    @Test

    void actualizarEstadoRepartidor_alCancelar_rechazaElEstadoYNoGuarda() {
        PedidoDTO.RequestActualizacionRepartidor dto = mock(PedidoDTO.RequestActualizacionRepartidor.class);
        Pedidos pedido = pedidoMock();
        when(pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(1L, 5L)).thenReturn(true);
        when(pedidosRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedido.getEstadoPedido()).thenReturn("EN_RUTA");
        when(dto.nuevoEstado()).thenReturn("CANCELADO");
        assertThatThrownBy(() -> pedidoService.actualizarEstadoRepartidor(1L, 5L, dto))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Estado no permitido");
        verify(dto, never()).applyTo(any());
        verify(pedidosRepository, never()).save(any());
        verify(pedido, never()).setFechaEntregaPedido(any());
    }

    @Test

    void actualizarEstadoRepartidor_cuandoPedidoNoEsSuyo_lanzaOperacionNoPermitida() {

        PedidoDTO.RequestActualizacionRepartidor dto = mock(PedidoDTO.RequestActualizacionRepartidor.class);

        when(pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(1L, 5L)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.actualizarEstadoRepartidor(1L, 5L, dto))

                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(pedidosRepository, never()).findById(any());

        verify(pedidosRepository, never()).save(any());

    }

    @Test

    void actualizarEstadoRepartidor_cuandoPedidoNoExiste_lanzaExcepcion() {

        PedidoDTO.RequestActualizacionRepartidor dto = mock(PedidoDTO.RequestActualizacionRepartidor.class);

        when(pedidosRepository.existsByIdPedidoAndDelivery_IdUsuario(1L, 5L)).thenReturn(true);

        when(pedidosRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.actualizarEstadoRepartidor(1L, 5L, dto))

                .isInstanceOf(RuntimeException.class);

        verify(pedidosRepository, never()).save(any());

    }

    // ---------- consultas ----------

    @Test

    void listarMisPedidos_retornaLosPedidosDelCliente() {

        when(pedidosRepository.findByCliente_IdUsuarioOrderByFechaPedidoDesc(1L))

                .thenReturn(List.of(pedidoMock(), pedidoMock()));

        List<PedidoDTO.ResponseCliente> resultado = pedidoService.listarMisPedidos(1L);

        assertThat(resultado).hasSize(2);

    }

    @Test

    void listarPedidosParaGestionar_consultaSoloPendientes() {

        when(pedidosRepository.findByEstadoPedidoOrderByFechaPedidoDesc("PENDIENTE"))

                .thenReturn(List.of(pedidoMock()));

        List<PedidoDTO.ResponseVendedor> resultado = pedidoService.listarPedidosParaGestionar();

        assertThat(resultado).hasSize(1);

        verify(pedidosRepository).findByEstadoPedidoOrderByFechaPedidoDesc("PENDIENTE");

    }

    @Test

    void listarPedidosAsignados_consultaSoloEstadosActivos() {

        List<String> estadosActivos = List.of("EN_RUTA", "PENDIENTE_ENTREGA");

        when(pedidosRepository.findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(5L, estadosActivos))

                .thenReturn(List.of(pedidoMock()));

        List<PedidoDTO.ResponseRepartidor> resultado = pedidoService.listarPedidosAsignados(5L);

        assertThat(resultado).hasSize(1);

    }

}
