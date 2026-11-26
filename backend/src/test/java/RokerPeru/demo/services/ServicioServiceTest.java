package RokerPeru.demo.services;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.ServicioRepository;
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
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private CodigoService codigoService;

    @InjectMocks
    private ServicioService servicioService;

    private Servicio servicioMock() {
        return mock(Servicio.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearServicio ----------

    @Test
    void crearServicio_conDatosValidos_guardaElServicio() {
        ServicioDTO.Request dto = mock(ServicioDTO.Request.class);
        Servicio servicio = servicioMock();

        when(dto.toEntity()).thenReturn(servicio);
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        ServicioDTO.Response response = servicioService.crearServicio(dto);

        assertThat(response).isNotNull();
        verify(servicioRepository).save(servicio);
    }

    // ---------- listar ----------

    @Test
    void listarServicio_retornaServiciosOrdenadosPorNombre() {
        when(servicioRepository.findAll(Sort.by("nombreServicio")))
                .thenReturn(List.of(servicioMock(), servicioMock()));

        List<ServicioDTO.Response> resultado = servicioService.listarServicio();

        assertThat(resultado).hasSize(2);
        verify(servicioRepository).findAll(Sort.by("nombreServicio"));
    }

    @Test
    void listarServicio_sinServicios_retornaListaVacia() {
        when(servicioRepository.findAll(Sort.by("nombreServicio"))).thenReturn(List.of());

        assertThat(servicioService.listarServicio()).isEmpty();
    }

    // ---------- buscarPorIdServicio ----------

    @Test
    void buscarPorIdServicio_cuandoExiste_retornaElServicio() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioMock()));

        assertThat(servicioService.buscarPorIdServicio(1L)).isNotNull();
    }

    @Test
    void buscarPorIdServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.buscarPorIdServicio(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    // ---------- actualizarServicio ----------

    @Test
    void actualizarServicio_cuandoExiste_aplicaCambios() {
        ServicioDTO.Request dto = mock(ServicioDTO.Request.class);
        Servicio servicio = servicioMock();
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioDTO.Response response = servicioService.actualizarServicio(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(servicio);
    }

    @Test
    void actualizarServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        ServicioDTO.Request dto = mock(ServicioDTO.Request.class);
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.actualizarServicio(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(dto, never()).applyTo(any());
    }

    // ---------- eliminarServicio ----------

    @Test
    void eliminarServicio_cuandoExiste_loElimina() {
        Servicio servicio = servicioMock();
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        servicioService.eliminarServicio(1L);

        verify(servicioRepository).delete(servicio);
    }

    @Test
    void eliminarServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.eliminarServicio(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(servicioRepository, never()).delete(any());
    }

    // ---------- vista del cliente ----------

    @Test
    void listarServiciosCliente_retornaServiciosOrdenadosPorNombre() {
        when(servicioRepository.findAll(Sort.by("nombreServicio")))
                .thenReturn(List.of(servicioMock()));

        List<ServicioDTO.ClienteResponse> resultado = servicioService.listarServiciosCliente();

        assertThat(resultado).hasSize(1);
    }

    @Test
    void buscarServiciosCliente_recortaEspaciosDelTexto() {
        when(servicioRepository.buscarServicio("limpieza")).thenReturn(List.of(servicioMock()));

        List<ServicioDTO.ClienteResponse> resultado =
                servicioService.buscarServiciosCliente("  limpieza  ");

        assertThat(resultado).hasSize(1);
        verify(servicioRepository).buscarServicio("limpieza");
    }

    @Test
    void buscarServiciosCliente_sinCoincidencias_retornaListaVacia() {
        when(servicioRepository.buscarServicio("zzz")).thenReturn(List.of());

        assertThat(servicioService.buscarServiciosCliente("zzz")).isEmpty();
    }
}
