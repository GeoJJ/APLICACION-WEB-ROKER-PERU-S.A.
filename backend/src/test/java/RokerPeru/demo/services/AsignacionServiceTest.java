package RokerPeru.demo.services;

import RokerPeru.demo.dto.AsignacionDTO;
import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.AsignacionesRepository;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private AsignacionesRepository asignacionesRepository;

    @Mock
    private OrdenesServicioRepository ordenesRepository;

    @Mock
    private EmpleadosServicioRepository empleadosRepository;

    @InjectMocks
    private AsignacionService asignacionService;

    private Asignaciones asignacionMock() {
        return mock(Asignaciones.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearAsignacion ----------

    @Test
    void crearAsignacion_conDatosValidos_guardaLaAsignacion() {
        AsignacionDTO.RequestAsignacion dto = mock(AsignacionDTO.RequestAsignacion.class);
        OrdenesServicio orden = mock(OrdenesServicio.class);
        EmpleadosServicio empleado = mock(EmpleadosServicio.class);
        Asignaciones asignacion = asignacionMock();

        when(dto.idOrdenServicio()).thenReturn(1L);
        when(dto.idEmpleado()).thenReturn(2L);
        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(empleadosRepository.findById(2L)).thenReturn(Optional.of(empleado));
        when(asignacionesRepository
                .existsByOrdenServicio_IdOrdenServicioAndEmpleado_IdEmpleadoServicio(1L, 2L))
                .thenReturn(false);
        when(dto.toEntity(orden, empleado)).thenReturn(asignacion);
        when(asignacionesRepository.save(asignacion)).thenReturn(asignacion);

        AsignacionDTO.Response response = asignacionService.crearAsignacion(dto);

        assertThat(response).isNotNull();
        verify(asignacionesRepository).save(asignacion);
    }

    @Test
    void crearAsignacion_conOrdenInexistente_lanzaRecursoNoEncontrado() {
        AsignacionDTO.RequestAsignacion dto = mock(AsignacionDTO.RequestAsignacion.class);
        when(dto.idOrdenServicio()).thenReturn(99L);
        when(ordenesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignacionService.crearAsignacion(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verifyNoInteractions(empleadosRepository);
        verify(asignacionesRepository, never()).save(any());
    }

    @Test
    void crearAsignacion_conEmpleadoInexistente_lanzaExcepcionYNoGuarda() {
        AsignacionDTO.RequestAsignacion dto = mock(AsignacionDTO.RequestAsignacion.class);
        when(dto.idOrdenServicio()).thenReturn(1L);
        when(dto.idEmpleado()).thenReturn(99L);
        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(mock(OrdenesServicio.class)));
        when(empleadosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignacionService.crearAsignacion(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");

        verify(asignacionesRepository, never()).save(any());
    }

    @Test
    void crearAsignacion_cuandoYaEstaAsignado_lanzaConflictoYNoGuarda() {
        AsignacionDTO.RequestAsignacion dto = mock(AsignacionDTO.RequestAsignacion.class);
        when(dto.idOrdenServicio()).thenReturn(1L);
        when(dto.idEmpleado()).thenReturn(2L);
        when(ordenesRepository.findById(1L)).thenReturn(Optional.of(mock(OrdenesServicio.class)));
        when(empleadosRepository.findById(2L)).thenReturn(Optional.of(mock(EmpleadosServicio.class)));
        when(asignacionesRepository
                .existsByOrdenServicio_IdOrdenServicioAndEmpleado_IdEmpleadoServicio(1L, 2L))
                .thenReturn(true);

        assertThatThrownBy(() -> asignacionService.crearAsignacion(dto))
                .isInstanceOf(ConflictoException.class);

        verify(asignacionesRepository, never()).save(any());
    }

    // ---------- listados ----------

    @Test
    void listarAsignacionesPorOrden_retornaLasAsignacionesDeLaOrden() {
        when(asignacionesRepository.findByOrdenServicio_IdOrdenServicioOrderByFechaAsignacionDesc(1L))
                .thenReturn(List.of(asignacionMock(), asignacionMock()));

        List<AsignacionDTO.Response> resultado = asignacionService.listarAsignacionesPorOrden(1L);

        assertThat(resultado).hasSize(2);
    }

    @Test
    void listarAsignacionesPorOrden_sinAsignaciones_retornaListaVacia() {
        when(asignacionesRepository.findByOrdenServicio_IdOrdenServicioOrderByFechaAsignacionDesc(1L))
                .thenReturn(List.of());

        assertThat(asignacionService.listarAsignacionesPorOrden(1L)).isEmpty();
    }

    @Test
    void listarAsignacionesPorEmpleado_retornaLasAsignacionesDelEmpleado() {
        when(asignacionesRepository.findByEmpleado_IdEmpleadoServicioOrderByFechaAsignacionDesc(2L))
                .thenReturn(List.of(asignacionMock()));

        assertThat(asignacionService.listarAsignacionesPorEmpleado(2L)).hasSize(1);
    }

    @Test
    void listarTodasAsignaciones_retornaTodas() {
        when(asignacionesRepository.findAllByOrderByFechaAsignacionDesc())
                .thenReturn(List.of(asignacionMock(), asignacionMock(), asignacionMock()));

        assertThat(asignacionService.listarTodasAsignaciones()).hasSize(3);
    }

    // ---------- buscarAsignacionPorId ----------

    @Test
    void buscarAsignacionPorId_cuandoExiste_retornaLaAsignacion() {
        when(asignacionesRepository.findById(1L)).thenReturn(Optional.of(asignacionMock()));

        assertThat(asignacionService.buscarAsignacionPorId(1L)).isNotNull();
    }

    @Test
    void buscarAsignacionPorId_cuandoNoExiste_lanzaExcepcion() {
        when(asignacionesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignacionService.buscarAsignacionPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }
}