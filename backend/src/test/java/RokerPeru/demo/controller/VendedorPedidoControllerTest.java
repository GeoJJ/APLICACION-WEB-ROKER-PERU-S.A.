package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
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
@WebMvcTest(VendedorPedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class VendedorPedidoControllerTest {
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
    private PedidoService pedidoService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String URL = "/vendedor/pedidos";

    private static final String ASIGNACION_VALIDA = """
            {"idDelivery": 3}
            """;

    private PedidoDTO.ResponseVendedor respuesta(String estado) {
        return new PedidoDTO.ResponseVendedor(1L, null, null, "B001", "B001-00000001", null, null, null, null, estado, 30.0, "EFECTIVO", 10L, "Cliente Uno", "Av. Lima 123", 2L, "Vendedor Uno", 3L, "Repartidor Uno", List.of());
    }

    // ---------- GET /vendedor/pedidos ----------

    @Test
    void listarPedidosParaGestionar_retorna200YLaLista() throws Exception {
        when(pedidoService.listarPedidosParaGestionar())
                .thenReturn(List.of(respuesta("PENDIENTE"), respuesta("PENDIENTE")));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estadoPedido").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].nombreCliente").value("Cliente Uno"));
    }

    @Test
    void listarPedidosParaGestionar_sinPedidos_retornaListaVacia() throws Exception {
        when(pedidoService.listarPedidosParaGestionar()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------- PUT /vendedor/pedidos/{idPedido}/confirmar ----------

    @Test
    void confirmarYAsignarPedido_conDatosValidos_retorna200YElPedidoEnRuta() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(eq(1L), any(PedidoDTO.RequestAsignacionVendedor.class), eq(2L)))
                .thenReturn(respuesta("EN_RUTA"));

        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoPedido").value("EN_RUTA"))
                .andExpect(jsonPath("$.idDelivery").value(3))
                .andExpect(jsonPath("$.idVendedor").value(2));
    }

    @Test
    void confirmarYAsignarPedido_enviaAlServiceLosIdsYElRepartidorCorrectos() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(any(), any(), any()))
                .thenReturn(respuesta("EN_RUTA"));

        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().isOk());

        ArgumentCaptor<PedidoDTO.RequestAsignacionVendedor> captor =
                ArgumentCaptor.forClass(PedidoDTO.RequestAsignacionVendedor.class);
        verify(pedidoService).confirmarYAsignarPedido(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().idDelivery()).isEqualTo(3L);
    }

    @Test
    void confirmarYAsignarPedido_conFechasOpcionales_retorna200() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(any(), any(), any()))
                .thenReturn(respuesta("EN_RUTA"));

        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idDelivery": 3,
                                  "fechaSalidaPedido": "2026-11-20T10:00:00",
                                  "fechaEntregaPedido": "2026-11-20T14:00:00"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<PedidoDTO.RequestAsignacionVendedor> captor =
                ArgumentCaptor.forClass(PedidoDTO.RequestAsignacionVendedor.class);
        verify(pedidoService).confirmarYAsignarPedido(eq(1L), captor.capture(), eq(2L));
        assertThat(captor.getValue().fechaSalidaPedido()).isNotNull();
        assertThat(captor.getValue().fechaEntregaPedido()).isNotNull();
    }

    @Test
    void confirmarYAsignarPedido_sinIdDelivery_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void confirmarYAsignarPedido_sinParametro_usaVendedorAutenticado() throws Exception {
        mockMvc.perform(put(URL+"/1/confirmar").contentType(MediaType.APPLICATION_JSON).content(ASIGNACION_VALIDA)).andExpect(status().isOk());
        verify(pedidoService).confirmarYAsignarPedido(eq(1L), any(), eq(2L));
    }

    @Test
    void confirmarYAsignarPedido_conIdPedidoNoNumerico_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/abc/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void confirmarYAsignarPedido_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void confirmarYAsignarPedido_sinContentType_retorna415() throws Exception {
        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(pedidoService);
    }

    // ---------- errores que vienen del service ----------

    @Test
    void confirmarYAsignarPedido_cuandoElPedidoNoExiste_retorna404() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(eq(99L), any(), eq(2L)))
                .thenThrow(new RecursoNoEncontradoException("Pedido", 99L));

        mockMvc.perform(put(URL + "/99/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmarYAsignarPedido_cuandoNoEstaPendiente_retornaErrorDeCliente() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(eq(1L), any(), eq(2L)))
                .thenThrow(new OperacionNoPermitidaException(
                        "Solo se pueden procesar pedidos que estén en estado PENDIENTE"));

        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void confirmarYAsignarPedido_cuandoElUsuarioNoEsRepartidor_retornaErrorDeCliente() throws Exception {
        when(pedidoService.confirmarYAsignarPedido(eq(1L), any(), eq(2L)))
                .thenThrow(new ReglaNegocioException("El usuario seleccionado no tiene el rol de REPARTIDOR"));

        mockMvc.perform(put(URL + "/1/confirmar")
                        .param("idVendedor", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ASIGNACION_VALIDA))
                .andExpect(status().is4xxClientError());
    }
}
