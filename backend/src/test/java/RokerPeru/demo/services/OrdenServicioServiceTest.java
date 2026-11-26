package RokerPeru.demo.services;

import RokerPeru.demo.dto.OrdenServicioDTO;

import RokerPeru.demo.entities.OrdenesServicio;

import RokerPeru.demo.entities.Servicio;

import RokerPeru.demo.entities.Usuario;

import RokerPeru.demo.enums.TipoComprobanteEnum;

import RokerPeru.demo.exception.OperacionNoPermitidaException;

import RokerPeru.demo.exception.RecursoNoEncontradoException;

import RokerPeru.demo.exception.ReglaNegocioException;

import RokerPeru.demo.repository.OrdenesServicioRepository;

import RokerPeru.demo.repository.ServicioRepository;

import RokerPeru.demo.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;

import org.junit.jupiter.params.provider.CsvSource;

import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class OrdenServicioServiceTest {

    @Mock

    private OrdenesServicioRepository ordenesRepository;

    @Mock

    private ServicioRepository servicioRepository;

    @Mock

    private UsuarioRepository usuarioRepository;

    @Mock

    private ComprobanteService comprobanteService;

    @Mock
    private CodigoService codigoService;

    @InjectMocks

    private OrdenServicioService ordenServicioService;

    private OrdenesServicio ordenMock() {

        return mock(OrdenesServicio.class, RETURNS_DEEP_STUBS);

    }

    // ---------- crearOrdenServicio ----------

    @ParameterizedTest

    @CsvSource({

            "JURIDICA,FACTURA_SERVICIO",

            "NATURAL,BOLETA_SERVICIO"

    })

    void crearOrdenServicio_segunTipoDePersona_asignaComprobanteYEstadoPendiente(

            String tipoPersona, TipoComprobanteEnum tipoEsperado) {

        OrdenServicioDTO.RequestCliente dto = mock(OrdenServicioDTO.RequestCliente.class);

        Servicio servicio = mock(Servicio.class);

        Usuario cliente = mock(Usuario.class);

        OrdenesServicio orden = ordenMock();

        when(dto.idServicio()).thenReturn(1L);

        // El ID del cliente se recibe desde la autenticación.

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cliente));

        when(servicio.getPrecioServicio()).thenReturn(150.0);



        when(dto.toEntity(servicio, cliente)).thenReturn(orden);

        when(ordenesRepository.save(orden)).thenReturn(orden);

        OrdenServicioDTO.ResponseCliente response = ordenServicioService.crearOrdenServicio(dto, 2L);

        assertThat(response).isNotNull();

        verify(orden).setEstadoServicio("PENDIENTE");

        verify(orden).setFechaOrdenServicio(any(LocalDateTime.class));

        verify(orden).setMontoTotalServicio(150.0);

        verify(orden, never()).setSerieComprobanteServicio(any());

        verifyNoInteractions(comprobanteService);

        verify(ordenesRepository).save(orden);

    }

    @Test

    void crearOrdenServicio_conServicioInexistente_lanzaExcepcionYNoGuarda() {

        OrdenServicioDTO.RequestCliente dto = mock(OrdenServicioDTO.RequestCliente.class);

        when(dto.idServicio()).thenReturn(99L);

        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.crearOrdenServicio(dto, 99L))

                .isInstanceOf(RuntimeException.class)

                .hasMessageContaining("99");

        verifyNoInteractions(usuarioRepository, comprobanteService);

        verify(ordenesRepository, never()).save(any());

    }

    @Test

    void crearOrdenServicio_conClienteInexistente_lanzaExcepcionYNoGuarda() {

        OrdenServicioDTO.RequestCliente dto = mock(OrdenServicioDTO.RequestCliente.class);

        when(dto.idServicio()).thenReturn(1L);

        // El ID del cliente se recibe desde la autenticación.

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(mock(Servicio.class)));

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.crearOrdenServicio(dto, 99L))

                .isInstanceOf(RuntimeException.class)

                .hasMessageContaining("99");

        verifyNoInteractions(comprobanteService);

        verify(ordenesRepository, never()).save(any());

    }

    // ---------- confirmarOrdenServicio ----------

    @Test

    void confirmarOrdenServicio_conPersonalCompleto_confirmaLaOrden() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        Servicio servicio = mock(Servicio.class);

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("PENDIENTE");

        when(orden.getServicio()).thenReturn(servicio);

        when(servicio.getCantidadPersonalServicio()).thenReturn("2 personas");

        when(orden.getAsignaciones().size()).thenReturn(2);

        when(ordenesRepository.save(orden)).thenReturn(orden);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(comprobanteService.emitir(any())).thenReturn(new ComprobanteService.NumeroComprobante("B002", "00000001"));

        OrdenServicioDTO.ResponseVendedor response = ordenServicioService.confirmarOrdenServicio(1L, dto, 3L);

        assertThat(response).isNotNull();

        verify(dto).applyTo(orden);

        verify(ordenesRepository).save(orden);

    }

    @Test

    void confirmarOrdenServicio_conPersonalInsuficiente_lanzaReglaNegocio() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        Servicio servicio = mock(Servicio.class);

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("PENDIENTE");

        when(orden.getServicio()).thenReturn(servicio);

        when(servicio.getCantidadPersonalServicio()).thenReturn("3");

        when(orden.getAsignaciones().size()).thenReturn(1);

        assertThatThrownBy(() -> ordenServicioService.confirmarOrdenServicio(1L, dto, 3L))

                .isInstanceOf(ReglaNegocioException.class)

                .hasMessageContaining("3")

                .hasMessageContaining("1");

        verify(dto, never()).applyTo(any());

        verify(ordenesRepository, never()).save(any());

    }

    @Test

    void confirmarOrdenServicio_conCantidadNoNumerica_requiereUnaPersonaPorDefecto() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        Servicio servicio = mock(Servicio.class);

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("PENDIENTE");

        when(orden.getServicio()).thenReturn(servicio);

        when(servicio.getCantidadPersonalServicio()).thenReturn("varios");

        when(orden.getAsignaciones().size()).thenReturn(1);

        when(ordenesRepository.save(orden)).thenReturn(orden);

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(mock(Usuario.class)));
        when(comprobanteService.emitir(any())).thenReturn(new ComprobanteService.NumeroComprobante("B002", "00000001"));

        OrdenServicioDTO.ResponseVendedor response = ordenServicioService.confirmarOrdenServicio(1L, dto, 3L);

        assertThat(response).isNotNull();

        verify(dto).applyTo(orden);

    }

    @Test

    void confirmarOrdenServicio_sinAsignaciones_lanzaReglaNegocio() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        Servicio servicio = mock(Servicio.class);

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("PENDIENTE");

        when(orden.getServicio()).thenReturn(servicio);

        when(servicio.getCantidadPersonalServicio()).thenReturn("1");

        when(orden.getAsignaciones()).thenReturn(null);

        assertThatThrownBy(() -> ordenServicioService.confirmarOrdenServicio(1L, dto, 3L))

                .isInstanceOf(ReglaNegocioException.class);

        verify(ordenesRepository, never()).save(any());

    }

    @Test

    void confirmarOrdenServicio_cuandoNoEstaPendiente_lanzaOperacionNoPermitida() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("CONFIRMADO");

        assertThatThrownBy(() -> ordenServicioService.confirmarOrdenServicio(1L, dto, 3L))

                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(ordenesRepository, never()).save(any());

    }

    @Test

    void confirmarOrdenServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        when(ordenesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.confirmarOrdenServicio(99L, dto, 3L))

                .isInstanceOf(RecursoNoEncontradoException.class);

    }

    // ---------- cancelarOrdenServicio ----------

    @Test

    void cancelarOrdenServicio_cuandoNoEstaCompletada_aplicaCambiosYGuarda() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("PENDIENTE");

        when(ordenesRepository.save(orden)).thenReturn(orden);

        OrdenServicioDTO.ResponseVendedor response = ordenServicioService.cancelarOrdenServicio(1L, dto, 2L);

        assertThat(response).isNotNull();

        verify(dto).applyTo(orden);

        verify(ordenesRepository).save(orden);

    }

    @Test

    void cancelarOrdenServicio_cuandoYaEstaCompletada_lanzaOperacionNoPermitida() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        OrdenesServicio orden = ordenMock();

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));

        when(orden.getEstadoServicio()).thenReturn("COMPLETADO");

        assertThatThrownBy(() -> ordenServicioService.cancelarOrdenServicio(1L, dto, 2L))

                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(dto, never()).applyTo(any());

        verify(ordenesRepository, never()).save(any());

    }

    @Test

    void cancelarOrdenServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {

        OrdenServicioDTO.RequestGestionVendedor dto = mock(OrdenServicioDTO.RequestGestionVendedor.class);

        when(ordenesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.cancelarOrdenServicio(99L, dto, 2L))

                .isInstanceOf(RecursoNoEncontradoException.class);

    }

    // ---------- consultas ----------

    @Test

    void listarOrdenesServicioCliente_retornaLasOrdenesDelCliente() {

        when(ordenesRepository.findByCliente_IdUsuarioOrderByFechaOrdenServicioDesc(1L))

                .thenReturn(List.of(ordenMock(), ordenMock()));

        List<OrdenServicioDTO.ResponseCliente> resultado =

                ordenServicioService.listarOrdenesServicioCliente(1L);

        assertThat(resultado).hasSize(2);

    }

    @Test

    void listarOrdenesServicioVendedor_retornaTodasLasOrdenes() {

        when(ordenesRepository.findAllByOrderByFechaOrdenServicioDesc())

                .thenReturn(List.of(ordenMock()));

        assertThat(ordenServicioService.listarOrdenesServicioVendedor()).hasSize(1);

    }

    @Test

    void listarOrdenesServicioPersonal_consultaSoloEstadosActivos() {

        List<String> estadosActivos = List.of("CONFIRMADO", "EN_PROCESO");

        when(ordenesRepository.findByEstadoServicioInOrderByFechaOrdenServicioDesc(estadosActivos))

                .thenReturn(List.of(ordenMock()));

        List<OrdenServicioDTO.ResponseVendedor> resultado =

                ordenServicioService.listarOrdenesServicioPersonal();

        assertThat(resultado).hasSize(1);

        verify(ordenesRepository).findByEstadoServicioInOrderByFechaOrdenServicioDesc(estadosActivos);

    }

    @Test

    void buscarOrdenServicioCliente_cuandoLaOrdenEsSuya_retornaLaOrden() {

        when(ordenesRepository.existsByIdOrdenServicioAndCliente_IdUsuario(1L, 5L)).thenReturn(true);

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(ordenMock()));

        OrdenServicioDTO.ResponseCliente response =

                ordenServicioService.buscarOrdenServicioCliente(1L, 5L);

        assertThat(response).isNotNull();

    }

    @Test

    void buscarOrdenServicioCliente_cuandoNoEsSuya_lanzaOperacionNoPermitida() {

        when(ordenesRepository.existsByIdOrdenServicioAndCliente_IdUsuario(1L, 5L)).thenReturn(false);

        assertThatThrownBy(() -> ordenServicioService.buscarOrdenServicioCliente(1L, 5L))

                .isInstanceOf(OperacionNoPermitidaException.class);

        verify(ordenesRepository, never()).findById(any());

    }

    @Test

    void buscarOrdenServicioVendedor_cuandoExiste_retornaLaOrden() {

        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(ordenMock()));

        assertThat(ordenServicioService.buscarOrdenServicioVendedor(1L)).isNotNull();

    }

    @Test

    void buscarOrdenServicioVendedor_cuandoNoExiste_lanzaRecursoNoEncontrado() {

        when(ordenesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenServicioService.buscarOrdenServicioVendedor(99L))

                .isInstanceOf(RecursoNoEncontradoException.class);

    }

}
