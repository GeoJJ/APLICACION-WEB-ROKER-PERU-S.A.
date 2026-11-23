package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ProveedoresRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedoresRepository proveedoresRepository;

    @InjectMocks
    private ProductoService productoService;

    private static final String CODIGO = "P001";

    private Producto productoMock() {
        return mock(Producto.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearProducto ----------

    @Test
    void crearProducto_conDatosValidos_guardaElProducto() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        Proveedores proveedor = mock(Proveedores.class);
        Producto producto = productoMock();

        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(dto.idProveedor()).thenReturn(1L);
        when(productoRepository.existeCodigoDuplicado(CODIGO, null)).thenReturn(false);
        when(proveedoresRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(dto.toEntity(proveedor)).thenReturn(producto);
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoDTO.Response response = productoService.crearProducto(dto);

        assertThat(response).isNotNull();
        verify(productoRepository).save(producto);
    }

    @Test
    void crearProducto_conCodigoDuplicado_lanzaConflictoYNoGuarda() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(productoRepository.existeCodigoDuplicado(CODIGO, null)).thenReturn(true);

        assertThatThrownBy(() -> productoService.crearProducto(dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(CODIGO);

        verifyNoInteractions(proveedoresRepository);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearProducto_conProveedorInexistente_lanzaRecursoNoEncontrado() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(dto.idProveedor()).thenReturn(99L);
        when(productoRepository.existeCodigoDuplicado(CODIGO, null)).thenReturn(false);
        when(proveedoresRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crearProducto(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(productoRepository, never()).save(any());
    }

    // ---------- listar / buscar ----------

    @Test
    void listarProducto_retornaProductosOrdenadosPorNombre() {
        when(productoRepository.findAll(Sort.by("nombreProducto")))
                .thenReturn(List.of(productoMock(), productoMock()));

        List<ProductoDTO.Response> resultado = productoService.listarProducto();

        assertThat(resultado).hasSize(2);
        verify(productoRepository).findAll(Sort.by("nombreProducto"));
    }

    @Test
    void listarProducto_sinProductos_retornaListaVacia() {
        when(productoRepository.findAll(Sort.by("nombreProducto"))).thenReturn(List.of());

        assertThat(productoService.listarProducto()).isEmpty();
    }

    @Test
    void buscarPorIdProducto_cuandoExiste_retornaElProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock()));

        assertThat(productoService.buscarPorIdProducto(1L)).isNotNull();
    }

    @Test
    void buscarPorIdProducto_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.buscarPorIdProducto(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void buscarProducto_recortaEspaciosDelTexto() {
        when(productoRepository.buscarProducto("casco")).thenReturn(List.of(productoMock()));

        List<ProductoDTO.Response> resultado = productoService.buscarProducto("  casco  ");

        assertThat(resultado).hasSize(1);
        verify(productoRepository).buscarProducto("casco");
    }

    // ---------- actualizarProducto ----------

    @Test
    void actualizarProducto_conDatosValidos_aplicaCambios() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        Producto producto = productoMock();
        Proveedores proveedor = mock(Proveedores.class);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(dto.idProveedor()).thenReturn(2L);
        when(productoRepository.existeCodigoDuplicado(CODIGO, 1L)).thenReturn(false);
        when(proveedoresRepository.findById(2L)).thenReturn(Optional.of(proveedor));

        ProductoDTO.Response response = productoService.actualizarProducto(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(producto, proveedor);
    }

    @Test
    void actualizarProducto_conCodigoDeOtroProducto_lanzaConflicto() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock()));
        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(productoRepository.existeCodigoDuplicado(CODIGO, 1L)).thenReturn(true);

        assertThatThrownBy(() -> productoService.actualizarProducto(1L, dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(CODIGO);

        verify(dto, never()).applyTo(any(), any());
    }

    @Test
    void actualizarProducto_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizarProducto(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(productoRepository, never()).existeCodigoDuplicado(any(), any());
    }

    @Test
    void actualizarProducto_conProveedorInexistente_lanzaRecursoNoEncontrado() {
        ProductoDTO.Request dto = mock(ProductoDTO.Request.class);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock()));
        when(dto.codigoProducto()).thenReturn(CODIGO);
        when(dto.idProveedor()).thenReturn(99L);
        when(productoRepository.existeCodigoDuplicado(CODIGO, 1L)).thenReturn(false);
        when(proveedoresRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizarProducto(1L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(dto, never()).applyTo(any(), any());
    }

    // ---------- eliminarProducto ----------

    @Test
    void eliminarProducto_cuandoExiste_loElimina() {
        Producto producto = productoMock();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminarProducto(1L);

        verify(productoRepository).delete(producto);
    }

    @Test
    void eliminarProducto_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminarProducto(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(productoRepository, never()).delete(any());
    }

    // ---------- cambiarEstadoProducto ----------

    @Test
    void cambiarEstadoProducto_normalizaElEstadoAMayusculasSinEspacios() {
        Producto producto = productoMock();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoDTO.Response response = productoService.cambiarEstadoProducto(1L, "  activo ");

        assertThat(response).isNotNull();
        verify(producto).setEstadoProducto("ACTIVO");
    }

    @Test
    void cambiarEstadoProducto_conEstadoNulo_lanzaReglaNegocio() {
        assertThatThrownBy(() -> productoService.cambiarEstadoProducto(1L, null))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(productoRepository);
    }

    @Test
    void cambiarEstadoProducto_conEstadoEnBlanco_lanzaReglaNegocio() {
        assertThatThrownBy(() -> productoService.cambiarEstadoProducto(1L, "   "))
                .isInstanceOf(ReglaNegocioException.class);

        verifyNoInteractions(productoRepository);
    }

    @Test
    void cambiarEstadoProducto_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.cambiarEstadoProducto(99L, "ACTIVO"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---------- vista del cliente ----------

    @Test
    void listarProductosCliente_retornaProductosOrdenadosPorNombre() {
        when(productoRepository.findAll(Sort.by("nombreProducto")))
                .thenReturn(List.of(productoMock()));

        List<ProductoDTO.ClienteResponse> resultado = productoService.listarProductosCliente();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void obtenerProductoCliente_cuandoExiste_retornaElProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoMock()));

        assertThat(productoService.obtenerProductoCliente(1L)).isNotNull();
    }

    @Test
    void obtenerProductoCliente_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerProductoCliente(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}