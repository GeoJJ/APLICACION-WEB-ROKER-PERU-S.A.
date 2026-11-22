package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.PedidoService;
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
import org.springframework.http.MediaType;
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
@WebMvcTest(RepartidorPedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class RepartidorPedidoControllerTest {
    // El filtro se prueba aparte; el controlador recibe un principal autenticado.
    @BeforeEach
    void autenticarPrincipal() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn(5L);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }
    @AfterEach
    void limpiarPrincipal() { SecurityContextHolder.clearContext(); }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String URL = "/repartidor/pedidos";

    private PedidoDTO.ResponseRepartidor respuesta(String estado) {
        return new PedidoDTO.ResponseRepartidor(1L, null, estado, "Cliente Uno", "Av. Lima 123", "999888777", null, null);
    }

    private String json(String nuevoEstado) {
        return """
                {"nuevoEstado": %s}
                """.formatted(nuevoEstado);
    }

    // ---------- GET /repartidor/pedidos?idRepartidor= ----------

    @Test
    void listarPedidosAsignados_conPedidos_retorna200YLaLista() throws Exception {
        when(pedidoService.listarPedidosAsignados(5L))
                .thenReturn(List.of(respuesta("EN_RUTA"), respuesta("EN_RUTA")));

        mockMvc.perform(get(URL).param("idRepartidor", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreCliente").value("Cliente Uno"))
                .andExpect(jsonPath("$[0].direccionEntregaCliente").value("Av. Lima 123"))
                .andExpect(jsonPath("$[0].telefonoCliente").value("999888777"));

        verify(pedidoService).listarPedidosAsignados(5L);
    }

    @Test
    void listarPedidosAsignados_sinPedidos_retornaListaVacia() throws Exception {
        when(pedidoService.listarPedidosAsignados(5L)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("idRepartidor", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listarPedidosAsignados_sinParametro_usaPrincipal() throws Exception {
        when(pedidoService.listarPedidosAsignados(5L)).thenReturn(List.of());
        mockMvc.perform(get(URL)).andExpect(status().isOk());
        verify(pedidoService).listarPedidosAsignados(5L);
    }

    @Test
    void listarPedidosAsignados_ignoraIdEnParametro() throws Exception {
        when(pedidoService.listarPedidosAsignados(5L)).thenReturn(List.of());
        mockMvc.perform(get(URL).param("idRepartidor", "abc")).andExpect(status().isOk());
        verify(pedidoService).listarPedidosAsignados(5L);
    }

    // ---------- PUT /repartidor/pedidos/{idPedido}/estado ----------

    @Test
    void actualizarEstado_conEstadoValido_retorna200() throws Exception {
        when(pedidoService.actualizarEstadoRepartidor(
                eq(1L), eq(5L), any(PedidoDTO.RequestActualizacionRepartidor.class)))
                .thenReturn(respuesta("ENTREGADO"));

        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"entregado\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPedido").value("ENTREGADO"));
    }

    @Test
    void actualizarEstado_normalizaElEstadoAMayusculasYEnviaLosIds() throws Exception {
        when(pedidoService.actualizarEstadoRepartidor(any(), any(), any()))
                .thenReturn(respuesta("ENTREGADO"));

        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"  entregado  \"")))
                .andExpect(status().isOk());

        ArgumentCaptor<PedidoDTO.RequestActualizacionRepartidor> captor =
                ArgumentCaptor.forClass(PedidoDTO.RequestActualizacionRepartidor.class);
        verify(pedidoService).actualizarEstadoRepartidor(eq(1L), eq(5L), captor.capture());
        assertThat(captor.getValue().nuevoEstado()).isEqualTo("ENTREGADO");
    }

    @Test
    void actualizarEstado_conIncidente_loEnviaAlServiceSinEspacios() throws Exception {
        when(pedidoService.actualizarEstadoRepartidor(any(), any(), any()))
                .thenReturn(respuesta("CANCELADO"));

        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nuevoEstado": "CANCELADO", "incidentePedido": "  Cliente ausente  "}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<PedidoDTO.RequestActualizacionRepartidor> captor =
                ArgumentCaptor.forClass(PedidoDTO.RequestActualizacionRepartidor.class);
        verify(pedidoService).actualizarEstadoRepartidor(eq(1L), eq(5L), captor.capture());
        assertThat(captor.getValue().incidentePedido()).isEqualTo("Cliente ausente");
    }

    @Test
    void actualizarEstado_sinNuevoEstado_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void actualizarEstado_conEstadoEnBlanco_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"   \"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void actualizarEstado_sinParametro_usaPrincipal() throws Exception {
        mockMvc.perform(put(URL+"/1/estado").contentType(MediaType.APPLICATION_JSON).content("{\"nuevoEstado\":\"ENTREGADO\"}")).andExpect(status().isOk());
        verify(pedidoService).actualizarEstadoRepartidor(eq(1L), eq(5L), any());
    }

    @Test
    void actualizarEstado_conIdPedidoNoNumerico_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/abc/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"ENTREGADO\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void actualizarEstado_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void actualizarEstado_sinContentType_retorna415() throws Exception {
        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "5")
                        .content(json("\"ENTREGADO\"")))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(pedidoService);
    }

    // ---------- errores que vienen del service ----------

    @Test
    void actualizarEstado_cuandoElPedidoNoEsSuyo_retornaErrorDeCliente() throws Exception {
        when(pedidoService.actualizarEstadoRepartidor(eq(1L), eq(5L), any()))
                .thenThrow(new OperacionNoPermitidaException("Acceso denegado: Este pedido no está asignado a ti"));

        mockMvc.perform(put(URL + "/1/estado")
                        .param("idRepartidor", "9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"ENTREGADO\"")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void actualizarEstado_cuandoElPedidoNoExiste_retorna404() throws Exception {
        when(pedidoService.actualizarEstadoRepartidor(eq(99L), eq(5L), any()))
                .thenThrow(new RecursoNoEncontradoException("Pedido", 99L));

        mockMvc.perform(put(URL + "/99/estado")
                        .param("idRepartidor", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"ENTREGADO\"")))
                .andExpect(status().isNotFound());
    }
}
