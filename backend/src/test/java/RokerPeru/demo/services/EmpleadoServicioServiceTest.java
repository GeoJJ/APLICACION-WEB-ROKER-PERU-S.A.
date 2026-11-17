package RokerPeru.demo.services;

import RokerPeru.demo.dto.EmpleadoServicioDTO;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
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
class EmpleadoServicioServiceTest {

    @Mock
    private EmpleadosServicioRepository empleadosRepository;

    @InjectMocks
    private EmpleadoServicioService empleadoServicioService;

    private EmpleadosServicio empleadoMock() {
        return mock(EmpleadosServicio.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearEmpleadoServicio ----------

    @Test
    void crearEmpleadoServicio_conDatosValidos_guardaElEmpleado() {
        EmpleadoServicioDTO.Request dto = mock(EmpleadoServicioDTO.Request.class);
        EmpleadosServicio empleado = empleadoMock();

        when(dto.toEntity()).thenReturn(empleado);
        when(empleadosRepository.save(empleado)).thenReturn(empleado);

        EmpleadoServicioDTO.Response response = empleadoServicioService.crearEmpleadoServicio(dto);

        assertThat(response).isNotNull();
        verify(empleadosRepository).save(empleado);
    }

    // ---------- listar / buscar ----------

    @Test
    void listarEmpleadoServicio_retornaEmpleadosOrdenadosPorNombre() {
        when(empleadosRepository.findAll(Sort.by("nombreEmpleadoServicio")))
                .thenReturn(List.of(empleadoMock(), empleadoMock()));

        List<EmpleadoServicioDTO.Response> resultado = empleadoServicioService.listarEmpleadoServicio();

        assertThat(resultado).hasSize(2);
        verify(empleadosRepository).findAll(Sort.by("nombreEmpleadoServicio"));
    }

    @Test
    void listarEmpleadoServicio_sinEmpleados_retornaListaVacia() {
        when(empleadosRepository.findAll(Sort.by("nombreEmpleadoServicio"))).thenReturn(List.of());

        assertThat(empleadoServicioService.listarEmpleadoServicio()).isEmpty();
    }

    @Test
    void buscarEmpleadoServicio_recortaEspaciosDelTexto() {
        when(empleadosRepository.buscarEmpleadoServicio("juan")).thenReturn(List.of(empleadoMock()));

        List<EmpleadoServicioDTO.Response> resultado =
                empleadoServicioService.buscarEmpleadoServicio("  juan  ");

        assertThat(resultado).hasSize(1);
        verify(empleadosRepository).buscarEmpleadoServicio("juan");
    }

    @Test
    void buscarEmpleadoServicio_sinCoincidencias_retornaListaVacia() {
        when(empleadosRepository.buscarEmpleadoServicio("zzz")).thenReturn(List.of());

        assertThat(empleadoServicioService.buscarEmpleadoServicio("zzz")).isEmpty();
    }

    // ---------- actualizarEmpleadoServicio ----------

    @Test
    void actualizarEmpleadoServicio_cuandoExiste_aplicaCambios() {
        EmpleadoServicioDTO.Request dto = mock(EmpleadoServicioDTO.Request.class);
        EmpleadosServicio empleado = empleadoMock();
        when(empleadosRepository.findById(1L)).thenReturn(Optional.of(empleado));

        EmpleadoServicioDTO.Response response = empleadoServicioService.actualizarEmpleadoServicio(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(empleado);
    }

    @Test
    void actualizarEmpleadoServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        EmpleadoServicioDTO.Request dto = mock(EmpleadoServicioDTO.Request.class);
        when(empleadosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empleadoServicioService.actualizarEmpleadoServicio(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(dto, never()).applyTo(any());
    }

    // ---------- eliminarEmpleadoServicio ----------

    @Test
    void eliminarEmpleadoServicio_cuandoExiste_loElimina() {
        EmpleadosServicio empleado = empleadoMock();
        when(empleadosRepository.findById(1L)).thenReturn(Optional.of(empleado));

        empleadoServicioService.eliminarEmpleadoServicio(1L);

        verify(empleadosRepository).delete(empleado);
    }

    @Test
    void eliminarEmpleadoServicio_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(empleadosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empleadoServicioService.eliminarEmpleadoServicio(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(empleadosRepository, never()).delete(any());
    }
}