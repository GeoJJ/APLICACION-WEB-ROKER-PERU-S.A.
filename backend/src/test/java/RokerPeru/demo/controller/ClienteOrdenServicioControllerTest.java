package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.OrdenServicioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

@WebMvcTest(ClienteOrdenServicioController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteOrdenServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenServicioService ordenServicioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String URL = "/cliente/ordenes-servicio";
    private static final long ID_CLIENTE_LOGUEADO = 2L;

    // Fecha lejana para que el test no caduque con el tiempo (la fecha programada debe ser futura)
    private static final String ORDEN_VALIDA = """
            {
              "idServicio": 1,
              "fechaProgramadaServicio": "2099-12-01T09:00:00",
              "metodoPagoServicio": "yape",
              "direccionServicio": "Av. Ejemplo 123, Arequipa",
              "detalleSolicitudServicio": "Portón de acceso estrecho"
            }
            """;

    // El id del cliente ya no viene en el body ni en la URL: sale del usuario autenticado (token)
    @BeforeEach
    void autenticarCliente() {
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(ID_CLIENTE_LOGUEADO);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cliente, null, List.of()));
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private OrdenServicioDTO.ResponseCliente respuesta() {
        return new OrdenServicioDTO.ResponseCliente(
                1L, "ORD-0001", null, null,
                "B002", "00000001", null,
                "PENDIENTE", 150.0, "YAPE", null,
                "Limpieza general", "Por asignar", "Por asignar",
                "Av. Ejemplo 123, Arequipa", "Portón de acceso estrecho");
    }

    private String json(String idServicio, String fecha, String metodoPago, String direccion) {
        return """
                {
                  "idServicio": %s,
                  "fechaProgramadaServicio": %s,
                  "metodoPagoServicio": %s,
                  "direccionServicio": %s
                }
                """.formatted(idServicio, fecha, metodoPago, direccion);
    }

    // ---------- POST /cliente/ordenes-servicio ----------

    @Test
    void crearOrdenServicio_conDatosValidos_retorna201YLaOrdenCreada() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrdenServicio").value(1))
                .andExpect(jsonPath("$.codigoOrdenServicio").value("ORD-0001"))
                .andExpect(jsonPath("$.estadoServicio").value("PENDIENTE"))
                .andExpect(jsonPath("$.serieComprobanteServicio").value("B002"))
                .andExpect(jsonPath("$.numeroComprobanteServicio").value("00000001"))
                .andExpect(jsonPath("$.montoTotalServicio").value(150.0))
                .andExpect(jsonPath("$.nombreServicio").value("Limpieza general"));
    }

    @Test
    void crearOrdenServicio_usaElIdDelClienteAutenticadoYNormalizaLosDatos() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrdenServicioDTO.RequestCliente> captor =
                ArgumentCaptor.forClass(OrdenServicioDTO.RequestCliente.class);
        verify(ordenServicioService).crearOrdenServicio(captor.capture(), eq(ID_CLIENTE_LOGUEADO));

        assertThat(captor.getValue().idServicio()).isEqualTo(1L);
        assertThat(captor.getValue().metodoPagoServicio()).isEqualTo("YAPE");
        assertThat(captor.getValue().direccionServicio()).isEqualTo("Av. Ejemplo 123, Arequipa");
        assertThat(captor.getValue().detalleSolicitudServicio()).isEqualTo("Portón de acceso estrecho");
    }

    @Test
    void crearOrdenServicio_ignoraUnIdClienteEnviadoEnElBody() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenReturn(respuesta());

        // Un cliente no puede crear órdenes a nombre de otro: manda el token, no el body
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idServicio": 1,
                                  "idCliente": 99,
                                  "fechaProgramadaServicio": "2099-12-01T09:00:00",
                                  "metodoPagoServicio": "EFECTIVO",
                                  "direccionServicio": "Av. Ejemplo 123"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(ordenServicioService).crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO));
        verify(ordenServicioService, never()).crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(99L));
    }

    @Test
    void crearOrdenServicio_sinDetalleDeSolicitud_retorna201() throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"EFECTIVO\"", "\"Calle 1\"")))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"EFECTIVO", "yape", "Plin", "TARJETA", "transferencia"})
    void crearOrdenServicio_conCadaMetodoDePagoValido_retorna201(String metodo) throws Exception {
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"" + metodo + "\"", "\"Calle 1\"")))
                .andExpect(status().isCreated());
    }

    @Test
    void crearOrdenServicio_sinIdServicio_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("null", "\"2099-12-01T09:00:00\"", "\"EFECTIVO\"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_sinFechaProgramada_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "null", "\"EFECTIVO\"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conFechaProgramadaEnElPasado_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2020-01-01T09:00:00\"", "\"EFECTIVO\"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conFechaProgramadaConFormatoInvalido_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"mañana por la tarde\"", "\"EFECTIVO\"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_sinMetodoDePago_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "null", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conMetodoDePagoEnBlanco_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"   \"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conMetodoDePagoNoPermitido_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"BITCOIN\"", "\"Calle 1\"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_sinDireccion_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"EFECTIVO\"", "null")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conDireccionEnBlanco_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"EFECTIVO\"", "\"   \"")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }

    @Test
    void crearOrdenServicio_conDireccionMayorA255Caracteres_retorna400() throws Exception {
        String direccionLarga = "x".repeat(256);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("1", "\"2099-12-01T09:00:00\"", "\"EFECTIVO\"", "\"" + direccionLarga + "\"")))
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
        when(ordenServicioService.crearOrdenServicio(any(OrdenServicioDTO.RequestCliente.class), eq(ID_CLIENTE_LOGUEADO)))
                .thenThrow(new RecursoNoEncontradoException("Servicio", 1L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDEN_VALIDA))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /cliente/ordenes-servicio ----------

    @Test
    void listarOrdenesServicioCliente_conOrdenes_retorna200YLaListaDelClienteAutenticado() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(ID_CLIENTE_LOGUEADO))
                .thenReturn(List.of(respuesta(), respuesta()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreServicio").value("Limpieza general"));

        verify(ordenServicioService).listarOrdenesServicioCliente(ID_CLIENTE_LOGUEADO);
    }

    @Test
    void listarOrdenesServicioCliente_sinOrdenes_retornaListaVacia() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(ID_CLIENTE_LOGUEADO)).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listarOrdenesServicioCliente_ignoraUnIdClienteEnviadoPorParametro() throws Exception {
        when(ordenServicioService.listarOrdenesServicioCliente(ID_CLIENTE_LOGUEADO)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("idCliente", "99"))
                .andExpect(status().isOk());

        verify(ordenServicioService).listarOrdenesServicioCliente(ID_CLIENTE_LOGUEADO);
        verify(ordenServicioService, never()).listarOrdenesServicioCliente(99L);
    }

    // ---------- GET /cliente/ordenes-servicio/{idOrden} ----------

    @Test
    void buscarOrdenServicioCliente_cuandoEsSuya_retorna200YLaOrden() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(1L, ID_CLIENTE_LOGUEADO)).thenReturn(respuesta());

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrdenServicio").value(1))
                .andExpect(jsonPath("$.estadoServicio").value("PENDIENTE"))
                .andExpect(jsonPath("$.numeroComprobanteServicio").value("00000001"));

        verify(ordenServicioService).buscarOrdenServicioCliente(1L, ID_CLIENTE_LOGUEADO);
    }

    @Test
    void buscarOrdenServicioCliente_cuandoNoExiste_retorna404() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(99L, ID_CLIENTE_LOGUEADO))
                .thenThrow(new RecursoNoEncontradoException("Orden de servicio", 99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarOrdenServicioCliente_cuandoEsDeOtroCliente_retorna409() throws Exception {
        when(ordenServicioService.buscarOrdenServicioCliente(5L, ID_CLIENTE_LOGUEADO))
                .thenThrow(new OperacionNoPermitidaException(
                        "Acceso denegado: Esta orden no pertenece a este cliente"));

        mockMvc.perform(get(URL + "/5"))
                .andExpect(status().isConflict());
    }

    @Test
    void buscarOrdenServicioCliente_conIdOrdenNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenServicioService);
    }
}