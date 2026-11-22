package RokerPeru.demo.services;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.entities.MovimientosProducto;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.repository.MovimientosProductoRepository;
import RokerPeru.demo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientosProductoRepository movimientoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Producto productoConStock(int stock) {
        Producto producto = mock(Producto.class);
        when(producto.getStockProducto()).thenReturn(stock);
        return producto;
    }

    private MovimientoDTO.Request requestMock(Long idProducto, int cantidad) {
        MovimientoDTO.Request request = mock(MovimientoDTO.Request.class);
        when(request.productoId()).thenReturn(idProducto);
        when(request.cantidad()).thenReturn(cantidad);
        return request;
    }

    // ---------- registrarIngreso ----------

    @Test
    void registrarIngreso_sumaElStockYGuardaElMovimiento() {
        Producto producto = productoConStock(10);
        MovimientoDTO.Request request = requestMock(1L, 5);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        MovimientoDTO.Response response = inventarioService.registrarIngreso(request);

        assertThat(response).isNotNull();
        verify(producto).setStockProducto(15);

        ArgumentCaptor<MovimientosProducto> captor = ArgumentCaptor.forClass(MovimientosProducto.class);
        verify(movimientoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoMovimientoProducto()).isEqualTo("INGRESO");
        assertThat(captor.getValue().getCantidadMovimientoProducto()).isEqualTo(5);
        assertThat(captor.getValue().getFechaMovimientoProducto()).isNotNull();
    }

    @Test
    void registrarIngreso_cuandoProductoNoExiste_lanzaExcepcionYNoGuarda() {
        MovimientoDTO.Request request = mock(MovimientoDTO.Request.class);
        when(request.productoId()).thenReturn(99L);
        when(productoRepository.buscarParaActualizarStock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.registrarIngreso(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");

        verify(movimientoRepository, never()).save(any());
    }

    // ---------- registrarSalida ----------

    @Test
    void registrarSalida_restaElStockYGuardaElMovimiento() {
        Producto producto = productoConStock(10);
        MovimientoDTO.Request request = requestMock(1L, 3);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        MovimientoDTO.Response response = inventarioService.registrarSalida(request);

        assertThat(response).isNotNull();
        verify(producto).setStockProducto(7);

        ArgumentCaptor<MovimientosProducto> captor = ArgumentCaptor.forClass(MovimientosProducto.class);
        verify(movimientoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoMovimientoProducto()).isEqualTo("SALIDA");
    }

    @Test
    void registrarSalida_conCantidadIgualAlStock_dejaElStockEnCero() {
        Producto producto = productoConStock(5);
        MovimientoDTO.Request request = requestMock(1L, 5);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        inventarioService.registrarSalida(request);

        verify(producto).setStockProducto(0);
        verify(movimientoRepository).save(any(MovimientosProducto.class));
    }

    @Test
    void registrarSalida_conStockInsuficiente_lanzaExcepcionYNoGuarda() {
        Producto producto = productoConStock(2);
        MovimientoDTO.Request request = requestMock(1L, 5);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> inventarioService.registrarSalida(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(producto, never()).setStockProducto(anyInt());
        verify(movimientoRepository, never()).save(any());
    }

    // ---------- registrarAjuste ----------

    @Test
    void registrarAjuste_restaElStockYGuardaElMovimientoComoAjuste() {
        Producto producto = productoConStock(10);
        MovimientoDTO.Request request = requestMock(1L, 4);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        MovimientoDTO.Response response = inventarioService.registrarAjuste(request);

        assertThat(response).isNotNull();
        verify(producto).setStockProducto(6);

        ArgumentCaptor<MovimientosProducto> captor = ArgumentCaptor.forClass(MovimientosProducto.class);
        verify(movimientoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipoMovimientoProducto()).isEqualTo("AJUSTE");
    }

    @Test
    void registrarAjuste_conStockInsuficiente_lanzaExcepcionYNoGuarda() {
        Producto producto = productoConStock(1);
        MovimientoDTO.Request request = requestMock(1L, 10);
        when(productoRepository.buscarParaActualizarStock(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> inventarioService.registrarAjuste(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(movimientoRepository, never()).save(any());
    }

    // ---------- consultarStock ----------

    @Test
    void consultarStock_cuandoExiste_retornaElStock() {
        when(productoRepository.consultarStockPorId(1L)).thenReturn(Optional.of(25));

        assertThat(inventarioService.consultarStock(1L)).isEqualTo(25);
    }

    @Test
    void consultarStock_cuandoNoExiste_lanzaExcepcion() {
        when(productoRepository.consultarStockPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.consultarStock(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    // ---------- listarMovimientos ----------

    @Test
    void listarMovimientos_retornaElHistorialCompleto() {
        MovimientosProducto m1 = mock(MovimientosProducto.class, RETURNS_DEEP_STUBS);
        MovimientosProducto m2 = mock(MovimientosProducto.class, RETURNS_DEEP_STUBS);
        when(movimientoRepository.findAllByOrderByFechaMovimientoProductoDesc())
                .thenReturn(List.of(m1, m2));

        List<MovimientoDTO.Response> resultado = inventarioService.listarMovimientos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void listarMovimientos_sinMovimientos_retornaListaVacia() {
        when(movimientoRepository.findAllByOrderByFechaMovimientoProductoDesc()).thenReturn(List.of());

        assertThat(inventarioService.listarMovimientos()).isEmpty();
    }

    // ---------- consultarAlertasStock ----------

    @Test
    void consultarAlertasStock_retornaUnaAlertaPorProductoConBajoStock() {
        when(productoRepository.findProductosConBajoStock())
                .thenReturn(List.of(mock(Producto.class), mock(Producto.class)));

        List<MovimientoDTO.AlertaStockResponse> resultado = inventarioService.consultarAlertasStock();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void consultarAlertasStock_sinProductosEnBajoStock_retornaListaVacia() {
        when(productoRepository.findProductosConBajoStock()).thenReturn(List.of());

        assertThat(inventarioService.consultarAlertasStock()).isEmpty();
    }
}
