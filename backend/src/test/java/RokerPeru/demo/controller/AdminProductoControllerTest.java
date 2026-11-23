package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.security.JwtAuthenticationFilter;
import RokerPeru.demo.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String URL = "/admin/productos";

    private static final String PRODUCTO_VALIDO = """
            {
              "codigoProducto": "p001",
              "nombreProducto": "Casco de seguridad",
              "categoriaProducto": "seguridad",
              "precioProducto": 25.5,
              "stockProducto": 10,
              "stockMinimo": 5,
              "estadoProducto": "activo",
              "idProveedor": 1
            }
            """;

    private ProductoDTO.Response respuesta() {
        return new ProductoDTO.Response(
                1L, "P001", "Casco de seguridad", "SEGURIDAD",
                25.5, 10, 5, "ACTIVO", 1L, "Proveedor SAC");
    }

    private String json(String codigo, String nombre, String precio, String stock, String idProveedor) {
        return """
                {
                  "codigoProducto": %s,
                  "nombreProducto": %s,
                  "precioProducto": %s,
                  "stockProducto": %s,
                  "idProveedor": %s
                }
                """.formatted(codigo, nombre, precio, stock, idProveedor);
    }

    // ---------- POST /admin/productos ----------

    @Test
    void crearProducto_conDatosValidos_retorna201YElProducto() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.Request.class))).thenReturn(respuesta());

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.codigoProducto").value("P001"))
                .andExpect(jsonPath("$.precioProducto").value(25.5));

        verify(productoService).crearProducto(any(ProductoDTO.Request.class));
    }

    @Test
    void crearProducto_conCodigoDuplicado_retorna409() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.Request.class)))
                .thenThrow(new ConflictoException("Ya existe un producto con el código: P001"));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void crearProducto_conProveedorInexistente_retorna404() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.Request.class)))
                .thenThrow(new RecursoNoEncontradoException("Proveedor", 1L));

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearProducto_sinCodigo_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("null", "\"Casco\"", "10.0", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conCodigoEnBlanco_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"   \"", "\"Casco\"", "10.0", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conCodigoMayorA50Caracteres_retorna400() throws Exception {
        String codigoLargo = "\"" + "A".repeat(51) + "\"";

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(codigoLargo, "\"Casco\"", "10.0", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_sinNombre_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"P001\"", "null", "10.0", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_sinPrecio_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"P001\"", "\"Casco\"", "null", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conPrecioNegativo_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"P001\"", "\"Casco\"", "-1.0", "5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conStockNegativo_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"P001\"", "\"Casco\"", "10.0", "-5", "1")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_sinProveedor_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("\"P001\"", "\"Casco\"", "10.0", "5", "null")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void crearProducto_conJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    // ---------- GET /admin/productos ----------

    @Test
    void listarProducto_retorna200YLaLista() throws Exception {
        when(productoService.listarProducto()).thenReturn(List.of(respuesta(), respuesta()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombreProducto").value("Casco de seguridad"));
    }

    @Test
    void listarProducto_sinProductos_retornaListaVacia() throws Exception {
        when(productoService.listarProducto()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------- GET /admin/productos/buscar ----------

    @Test
    void buscarProducto_conTexto_retorna200() throws Exception {
        when(productoService.buscarProducto("casco")).thenReturn(List.of(respuesta()));

        mockMvc.perform(get(URL + "/buscar").param("texto", "casco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productoService).buscarProducto("casco");
    }

    @Test
    void buscarProducto_sinTexto_retorna400() throws Exception {
        mockMvc.perform(get(URL + "/buscar"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    // ---------- GET /admin/productos/{id} ----------

    @Test
    void buscarPorIdProducto_cuandoExiste_retorna200() throws Exception {
        when(productoService.buscarPorIdProducto(1L)).thenReturn(respuesta());

        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.nombreProveedor").value("Proveedor SAC"));
    }

    @Test
    void buscarPorIdProducto_cuandoNoExiste_retorna404() throws Exception {
        when(productoService.buscarPorIdProducto(99L))
                .thenThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorIdProducto_conIdNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    // ---------- PUT /admin/productos/{id} ----------

    @Test
    void actualizarProducto_conDatosValidos_retorna200() throws Exception {
        when(productoService.actualizarProducto(eq(1L), any(ProductoDTO.Request.class)))
                .thenReturn(respuesta());

        mockMvc.perform(put(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoProducto").value("P001"));

        verify(productoService).actualizarProducto(eq(1L), any(ProductoDTO.Request.class));
    }

    @Test
    void actualizarProducto_conDatosInvalidos_retorna400() throws Exception {
        mockMvc.perform(put(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    @Test
    void actualizarProducto_cuandoNoExiste_retorna404() throws Exception {
        when(productoService.actualizarProducto(eq(99L), any(ProductoDTO.Request.class)))
                .thenThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(put(URL + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProducto_conCodigoDeOtroProducto_retorna409() throws Exception {
        when(productoService.actualizarProducto(eq(1L), any(ProductoDTO.Request.class)))
                .thenThrow(new ConflictoException("Ya existe un producto con el código: P001"));

        mockMvc.perform(put(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUCTO_VALIDO))
                .andExpect(status().isConflict());
    }

    // ---------- PATCH /admin/productos/{id}/estado ----------

    @Test
    void cambiarEstadoProducto_conEstado_retorna200() throws Exception {
        when(productoService.cambiarEstadoProducto(1L, "INACTIVO")).thenReturn(respuesta());

        mockMvc.perform(patch(URL + "/1/estado").param("estado", "INACTIVO"))
                .andExpect(status().isOk());

        verify(productoService).cambiarEstadoProducto(1L, "INACTIVO");
    }

    @Test
    void cambiarEstadoProducto_sinParametroEstado_retorna400() throws Exception {
        mockMvc.perform(patch(URL + "/1/estado"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoService);
    }

    // ---------- DELETE /admin/productos/{id} ----------

    @Test
    void eliminarProducto_cuandoExiste_retorna204() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    void eliminarProducto_cuandoNoExiste_retorna404() throws Exception {
        doThrow(new RecursoNoEncontradoException("Producto", 99L))
                .when(productoService).eliminarProducto(99L);

        mockMvc.perform(delete(URL + "/99"))
                .andExpect(status().isNotFound());
    }
}