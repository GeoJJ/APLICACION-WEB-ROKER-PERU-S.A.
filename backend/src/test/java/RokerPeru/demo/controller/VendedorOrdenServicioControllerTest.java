package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.OrdenServicioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import RokerPeru.demo.entities.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(RokerPeru.demo.config.AuthenticatedControllerTestConfig.class)
@WebMvcTest(VendedorOrdenServicioController.class)
@AutoConfigureMockMvc(addFilters = false)
class VendedorOrdenServicioControllerTest {
    // El filtro se prueba aparte; el controlador recibe un principal autenticado.
    @BeforeEach
    void autenticarPrincipal() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn(2L);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }
    @AfterEach
    void limpiarPrincipal() { SecurityContextHolder.clearContext(); }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenServicioService ordenServicioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String URL = "/vendedor/ordenes-servicio";

    private OrdenServicioDTO.ResponseVendedor respuesta(String estado) {
        return new OrdenServicioDTO.ResponseVendedor(1L, null, null, null, "B002", "B002-00000001", null, estado, 150.0, "YAPE", null, "Limpieza general", 2, 2, 10L, "Cliente Uno", "999999999", 20L, "Vendedor Uno", 30L, "Auxiliar Uno", "Av. Los Proceres 245", null);
    }

    // ---------- GET /vendedor/ordenes-servicio ----------

    @Test
    void listarOrdenesServicioVendedor_retorna200YLaLista() throws Exception {
        when(ordenServicioService.listarOrdenesServicioVendedor())
                .thenReturn(List.of(respuesta("PENDIENTE"), respuesta("CONFIRMADO")));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreCliente").value("Cliente Uno"))
                .andExpect(jsonPath("$[0].cantidadPersonalRequerida").value(2));
    }

    @Test
    void listarOrdenesServicioVendedor_sinOrdenes_retornaListaVacia() throws Exception {
        when(ordenServicioService.listarOrdenesServicioVendedor()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------- GET /vendedor/ordenes-servicio/{idOrden} ----------

    @Test
    void buscarOrdenServicioVendedor_cuandoExiste_retorna200() throws Exception {
        when(ordenServicioService.buscarOrdenServicioVendedor(1L)).thenReturn(respuesta("PENDIENTE"));

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrdenServicio").value(1))
                .andExpect(jsonPath("$.estadoServicio").value("PENDIENTE"))
                .andExpect(jsonPath("$.cantidadPersonalAsignada").value(2));
    }

    @Test
    void buscarOrdenServicioVendedor_cuandoNoExiste_retorna404() throws Exception {
        when(ordenServicioService.buscarOrdenServicioVendedor(99L))
                .thenThrow(new RecursoNoEncontradoException("Orden de servicio", 99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarOrdenServicioVendedor_conIdNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    // ---------- PUT /vendedor/ordenes-servicio/{idOrden}/confirmar ----------

    @Test
    void confirmarOrdenServicio_sinIncidencia_retorna200YEnviaEstadoConfirmado() throws Exception {
        when(ordenServicioService.confirmarOrdenServicio(eq(1L), any(OrdenServicioDTO.RequestGestionVendedor.class), eq(2L)))
                .thenReturn(respuesta("CONFIRMADO"));

        mockMvc.perform(put(URL + "/1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoServicio").value("CONFIRMADO"));

        ArgumentCaptor<OrdenServicioDTO.RequestGestionVendedor> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestGestionVendedor.class);
        verify(ordenServicioService).confirmarOrdenServicio(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().nuevoEstado()).isEqualTo("CONFIRMADO");
        assertThat(captor.getValue().incidenciaServicio()).isNull();
    }

    @Test
    void confirmarOrdenServicio_conIncidencia_laEnviaAlServiceSinEspacios() throws Exception {
        when(ordenServicioService.confirmarOrdenServicio(eq(1L), any(OrdenServicioDTO.RequestGestionVendedor.class), eq(2L)))
                .thenReturn(respuesta("CONFIRMADO"));

        mockMvc.perform(put(URL + "/1/confirmar").param("incidencia", "  Cliente pidió cambio de hora  "))
                .andExpect(status().isOk());

        ArgumentCaptor<OrdenServicioDTO.RequestGestionVendedor> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestGestionVendedor.class);
        verify(ordenServicioService).confirmarOrdenServicio(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().incidenciaServicio()).isEqualTo("Cliente pidió cambio de hora");
    }

    @Test
    void confirmarOrdenServicio_cuandoNoExiste_retorna404() throws Exception {
        when(ordenServicioService.confirmarOrdenServicio(eq(99L), any(), eq(2L)))
                .thenThrow(new RecursoNoEncontradoException("Orden de servicio", 99L));

        mockMvc.perform(put(URL + "/99/confirmar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmarOrdenServicio_conPersonalInsuficiente_retornaErrorDeCliente() throws Exception {
        when(ordenServicioService.confirmarOrdenServicio(eq(1L), any(), eq(2L)))
                .thenThrow(new ReglaNegocioException(
                        "No se puede confirmar la orden. El servicio requiere 3 personas, pero solo se han asignado 1."));

        mockMvc.perform(put(URL + "/1/confirmar"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void confirmarOrdenServicio_cuandoNoEstaPendiente_retornaErrorDeCliente() throws Exception {
        when(ordenServicioService.confirmarOrdenServicio(eq(1L), any(), eq(2L)))
                .thenThrow(new OperacionNoPermitidaException(
                        "Solo se pueden confirmar órdenes en estado PENDIENTE"));

        mockMvc.perform(put(URL + "/1/confirmar"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void confirmarOrdenServicio_conIdNoNumerico_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/abc/confirmar"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    // ---------- PUT /vendedor/ordenes-servicio/{idOrden}/cancelar ----------

    @Test
    void cancelarOrdenServicio_sinIncidencia_retorna200YEnviaEstadoCancelado() throws Exception {
        when(ordenServicioService.cancelarOrdenServicio(eq(1L), any(OrdenServicioDTO.RequestGestionVendedor.class), eq(2L)))
                .thenReturn(respuesta("CANCELADO"));

        mockMvc.perform(put(URL + "/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoServicio").value("CANCELADO"));

        ArgumentCaptor<OrdenServicioDTO.RequestGestionVendedor> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestGestionVendedor.class);
        verify(ordenServicioService).cancelarOrdenServicio(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().nuevoEstado()).isEqualTo("CANCELADO");
        assertThat(captor.getValue().incidenciaServicio()).isNull();
    }

    @Test
    void cancelarOrdenServicio_conIncidencia_laEnviaAlService() throws Exception {
        when(ordenServicioService.cancelarOrdenServicio(eq(1L), any(OrdenServicioDTO.RequestGestionVendedor.class), eq(2L)))
                .thenReturn(respuesta("CANCELADO"));

        mockMvc.perform(put(URL + "/1/cancelar").param("incidencia", "Cliente no responde"))
                .andExpect(status().isOk());

        ArgumentCaptor<OrdenServicioDTO.RequestGestionVendedor> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestGestionVendedor.class);
        verify(ordenServicioService).cancelarOrdenServicio(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().incidenciaServicio()).isEqualTo("Cliente no responde");
    }

    @Test
    void cancelarOrdenServicio_cuandoNoExiste_retorna404() throws Exception {
        when(ordenServicioService.cancelarOrdenServicio(eq(99L), any(), eq(2L)))
                .thenThrow(new RecursoNoEncontradoException("Orden de servicio", 99L));

        mockMvc.perform(put(URL + "/99/cancelar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarOrdenServicio_cuandoYaEstaCompletada_retornaErrorDeCliente() throws Exception {
        when(ordenServicioService.cancelarOrdenServicio(eq(1L), any(), eq(2L)))
                .thenThrow(new OperacionNoPermitidaException("No se puede cancelar una orden ya completada"));

        mockMvc.perform(put(URL + "/1/cancelar"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void cancelarOrdenServicio_conIdNoNumerico_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/abc/cancelar"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }
}
