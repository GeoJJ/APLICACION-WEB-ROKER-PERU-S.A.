package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoDTO;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import RokerPeru.demo.entities.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(RokerPeru.demo.config.AuthenticatedControllerTestConfig.class)
@WebMvcTest(ClientePedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientePedidoControllerTest {
    // El filtro se prueba aparte; el controlador recibe un principal autenticado.
    @BeforeEach
    void autenticarPrincipal() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn(1L);
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

    private static final String URL = "/cliente/pedidos";

    private static final String PEDIDO_VALIDO = """
            {
              "metodoPagoPedido": "efectivo",
              "idCliente": 1,
              "detalles": [
                {"idProducto": 5, "cantidadProducto": 2}
              ]
            }
            """;

    private PedidoDTO.ResponseCliente respuestaCliente() {
        return new PedidoDTO.ResponseCliente(1L, null, null, "B001", "B001-00000001", null, null, null, null, "PENDIENTE", 30.0, "EFECTIVO", "Por asignar", "Por asignar", List.of());
    }

    // ---------- POST /cliente/pedidos ----------

    @Test
    void crearPedido_conDatosValidos_retorna201YElPedidoCreado() throws Exception {
        when(pedidoService.crearPedido(any(PedidoDTO.RequestCliente.class), eq(1L)))
                .thenReturn(respuestaCliente());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoPedido").value("PENDIENTE"))
                .andExpect(jsonPath("$.numeroComprobantePedido").value("B001-00000001"))
                .andExpect(jsonPath("$.montoTotalPedido").value(30.0));

        verify(pedidoService).crearPedido(any(PedidoDTO.RequestCliente.class), eq(1L));
    }

    @Test
    void crearPedido_sinMetodoDePago_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idCliente": 1, "detalles": []}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_conMetodoDePagoEnBlanco_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"metodoPagoPedido": "   ", "idCliente": 1, "detalles": []}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_sinIdCliente_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"metodoPagoPedido": "EFECTIVO", "detalles": []}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_sinDetalles_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"metodoPagoPedido": "EFECTIVO", "idCliente": 1}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_conCuerpoVacio_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void crearPedido_sinContentType_retorna415() throws Exception {
        mockMvc.perform(post(URL).content(PEDIDO_VALIDO))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(pedidoService);
    }

    // ---------- GET /cliente/pedidos?idCliente= ----------

    @Test
    void listarMisPedidos_conPedidos_retorna200YLaLista() throws Exception {
        when(pedidoService.listarMisPedidos(1L)).thenReturn(List.of(respuestaCliente()));

        mockMvc.perform(get(URL).param("idCliente", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoPedido").value("PENDIENTE"));

        verify(pedidoService).listarMisPedidos(1L);
    }

    @Test
    void listarMisPedidos_sinPedidos_retornaListaVacia() throws Exception {
        when(pedidoService.listarMisPedidos(1L)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("idCliente", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listarMisPedidos_sinParametro_usaClienteAutenticado() throws Exception {
        when(pedidoService.listarMisPedidos(1L)).thenReturn(List.of());
        mockMvc.perform(get(URL)).andExpect(status().isOk());
        verify(pedidoService).listarMisPedidos(1L);
    }

    @Test
    void listarMisPedidos_ignoraParametroAjeno() throws Exception {
        when(pedidoService.listarMisPedidos(1L)).thenReturn(List.of());
        mockMvc.perform(get(URL).param("idCliente", "abc")).andExpect(status().isOk());
        verify(pedidoService).listarMisPedidos(1L);
    }
}
