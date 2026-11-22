package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@org.springframework.context.annotation.Import(RokerPeru.demo.config.AuthenticatedControllerTestConfig.class)
@WebMvcTest(ClienteOrdenServicioController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteOrdenServicioControllerTest {
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

    private static final String URL = "/cliente/ordenes-servicio";

    private static final String ORDEN_VALIDA = """
            {
              "idServicio": 1,
              "idCliente": 2,
              "fechaProgramadaServicio": "2099-12-01T09:00:00",
              "metodoPagoServicio": "yape",
              "direccionServicio": "Av. Los Proceres 245",
              "detalleSolicitudServicio": "Portón de acceso estrecho"
            }
            """;

    private OrdenServicioDTO.ResponseCliente respuesta() {
        return new OrdenServicioDTO.ResponseCliente(1L, null, null, null, "B002", "B002-00000001", null, "PENDIENTE", 150.0, "YAPE", null, "Limpieza general", "Por asignar", "Por asignar", "Av. Los Proceres 245", null);
    }

    private String json(String idServicio, String idCliente, String metodoPago) {
        return """
                {
                  "idServicio": %s,
                  "idCliente": %s,
                  "fechaProgramadaServicio": "2099-12-01T09:00:00",
                  "direccionServicio": "Av. Los Proceres 245",
                  "metodoPagoServicio": %s
                }
                """.formatted(idServicio, idCliente, metodoPago);
    }

    // ---------- POST /cliente/ordenes-servicio ----------

    @Test
    void crearOrdenServicio_conDatosValidos_retorna201YLaOrdenCreada() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(2L)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrdenServicio").value(1))
                .andExpect(jsonPath("$.estadoServicio").value("PENDIENTE"))
                .andExpect(jsonPath("$.numeroComprobanteServicio").value("B002-00000001"))
                .andExpect(jsonPath("$.montoTotalServicio").value(150.0))
                .andExpect(jsonPath("$.nombreServicio").value("Limpieza general"));
    }

    @Test
    void crearOrdenServicio_normalizaElMetodoDePagoAMayusculas() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(2L)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrdenServicioDTO.RequestCliente> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestCliente.class);
        verify(ordenServicioService).crearOrdenServicio(captor.capture(), eq(2L));

        assertThat(captor.getValue().idServicio()).isEqualTo(1L);
        // La identidad del cliente procede del principal autenticado, no del DTO.
        assertThat(captor.getValue().metodoPagoServicio()).isEqualTo("YAPE");
    }

    @Test
    void crearOrdenServicio_sinCamposOpcionales_retorna201() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(2L)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "2", "\"EFECTIVO\"")))
                .andExpect(status().isCreated());
    }

    @Test
    void crearOrdenServicio_sinIdServicio_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("null", "2", "\"EFECTIVO\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_sinIdEnCuerpo_usaClienteAutenticado() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(), eq(2L))).thenReturn(respuesta());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1", "null", "\"YAPE\""))).andExpect(status().isCreated());
        verify(ordenServicioService).crearOrdenServicio(any(), eq(2L));
    }

    @Test
    void crearOrdenServicio_sinMetodoDePago_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "2", "null")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conMetodoDePagoEnBlanco_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "2", "\"   \"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conFechaProgramadaConFormatoInvalido_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idServicio": 1,
                                  "idCliente": 2,
                                  "fechaProgramadaServicio": "mañana por la tarde",
                                  "metodoPagoServicio": "EFECTIVO"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conCuerpoVacio_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_sinContentType_retorna415() throws Exception {
        mockMvc.perform(post(URL).content(ORDEN_VALIDA))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conServicioInexistente_retorna404() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(2L)))
                .thenThrow(new RecursoNoEncontradoException("Servicio", 1L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /cliente/ordenes-servicio?idCliente= ----------

    @Test
    void listarOrdenesServicioCliente_conOrdenes_retorna200YLaLista() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(2L))
                .thenReturn(List.of(respuesta(), respuesta()));

        mockMvc.perform(get(URL).param("idCliente", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreServicio").value("Limpieza general"));

        verify(ordenServicioService).listarOrdenesServicioCliente(2L);
    }

    @Test
    void listarOrdenesServicioCliente_sinOrdenes_retornaListaVacia() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(2L)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("idCliente", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listarOrdenesServicioCliente_sinParametro_usaPrincipal() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(2L)).thenReturn(List.of());
        mockMvc.perform(get(URL)).andExpect(status().isOk());
        verify(ordenServicioService).listarOrdenesServicioCliente(2L);
    }

    @Test
    void listarOrdenesServicioCliente_ignoraIdEnParametro() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(2L)).thenReturn(List.of());
        mockMvc.perform(get(URL).param("idCliente", "abc")).andExpect(status().isOk());
        verify(ordenServicioService).listarOrdenesServicioCliente(2L);
    }

    // ---------- GET /cliente/ordenes-servicio/{idOrden}?idCliente= ----------

    @Test
    void buscarOrdenServicioCliente_cuandoEsSuya_retorna200YLaOrden() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(1L, 2L)).thenReturn(respuesta());

        mockMvc.perform(get(URL + "/1").param("idCliente", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrdenServicio").value(1))
                .andExpect(jsonPath("$.estadoServicio").value("PENDIENTE"));

        verify(ordenServicioService).buscarOrdenServicioCliente(1L, 2L);
    }

    @Test
    void buscarOrdenServicioCliente_cuandoNoExiste_retorna404() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(99L, 2L))
                .thenThrow(new RecursoNoEncontradoException("Orden de servicio", 99L));

        mockMvc.perform(get(URL + "/99").param("idCliente", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarOrdenServicioCliente_cuandoEsDeOtroCliente_retornaErrorDeCliente() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(1L, 2L))
                .thenThrow(new OperacionNoPermitidaException(
                        "Acceso denegado: Esta orden no pertenece a este cliente"));

        mockMvc.perform(get(URL + "/1").param("idCliente", "5"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void buscarOrdenServicioCliente_sinParametro_usaPrincipal() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(1L, 2L)).thenReturn(respuesta());
        mockMvc.perform(get(URL+"/1")).andExpect(status().isOk());
        verify(ordenServicioService).buscarOrdenServicioCliente(1L, 2L);
    }

    @Test
    void buscarOrdenServicioCliente_conIdOrdenNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc").param("idCliente", "2"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }
}
