package RokerPeru.demo.services;

import static org.junit.jupiter.api.Assertions.*;

import RokerPeru.demo.dto.RolDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.RolRepository;
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
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    private static final String NOMBRE = "VENDEDOR";

    private Rol nuevoRolMock() {
        return mock(Rol.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearRol ----------
    // Verifica que al crear un rol con un nombre nuevo, este se guarde y se retorne la respuesta correctamente.
    @Test
    void crearRol_conNombreNuevo_guardaYRetornaRespuesta() {
        RolDTO.Request dto = mock(RolDTO.Request.class);
        Rol rol = nuevoRolMock();

        when(dto.nombreRol()).thenReturn(NOMBRE);
        when(rolRepository.existsByNombreRolIgnoreCase(NOMBRE)).thenReturn(false);
        when(dto.toEntity()).thenReturn(rol);
        when(rolRepository.save(rol)).thenReturn(rol);

        RolDTO.Response response = rolService.crearRol(dto);

        assertThat(response).isNotNull();
        verify(rolRepository).save(rol);
    }
    // Comprueba que si se intenta crear un rol con un nombre que ya existe, se lanza una excepción de conflicto y no se guarda.
    @Test
    void crearRol_conNombreDuplicado_lanzaConflictoYNoGuarda() {
        RolDTO.Request dto = mock(RolDTO.Request.class);
        when(dto.nombreRol()).thenReturn(NOMBRE);
        when(rolRepository.existsByNombreRolIgnoreCase(NOMBRE)).thenReturn(true);

        assertThatThrownBy(() -> rolService.crearRol(dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(NOMBRE);

        verify(rolRepository, never()).save(any());
    }

    // ---------- listarRol ----------
    // Verifica que se retorne la lista completa con todos los roles registrados en el sistema.
    @Test
    void listarRol_retornaTodosLosRoles() {
        when(rolRepository.findAll()).thenReturn(List.of(nuevoRolMock(), nuevoRolMock()));

        List<RolDTO.Response> resultado = rolService.listarRol();

        assertThat(resultado).hasSize(2);
    }
    // Comprueba que si no existen roles guardados, se retorna una lista vacía.
    @Test
    void listarRol_cuandoNoHayRoles_retornaListaVacia() {
        when(rolRepository.findAll()).thenReturn(List.of());

        assertThat(rolService.listarRol()).isEmpty();
    }

    // ---------- actualizarRol ----------
    // Verifica que al actualizar un rol con datos válidos, se apliquen los cambios correctamente.
    @Test
    void actualizarRol_conDatosValidos_aplicaCambios() {
        RolDTO.Request dto = mock(RolDTO.Request.class);
        Rol rol = nuevoRolMock();

        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(dto.nombreRol()).thenReturn(NOMBRE);
        when(rolRepository.existsByNombreRolIgnoreCaseAndIdRolNot(NOMBRE, 1L)).thenReturn(false);

        RolDTO.Response response = rolService.actualizarRol(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(rol);
    }
    // Comprueba que si se intenta actualizar un rol usando el nombre de otro rol existente, se lanza una excepción de conflicto.
    @Test
    void actualizarRol_conNombreDeOtroRol_lanzaConflicto() {
        RolDTO.Request dto = mock(RolDTO.Request.class);

        when(rolRepository.findById(1L)).thenReturn(Optional.of(nuevoRolMock()));
        when(dto.nombreRol()).thenReturn(NOMBRE);
        when(rolRepository.existsByNombreRolIgnoreCaseAndIdRolNot(NOMBRE, 1L)).thenReturn(true);

        assertThatThrownBy(() -> rolService.actualizarRol(1L, dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(NOMBRE);

        verify(dto, never()).applyTo(any());
    }
    // Valida que al intentar actualizar un rol cuyo ID no existe en la base de datos, se lance RecursoNoEncontradoException.
    @Test
    void actualizarRol_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        RolDTO.Request dto = mock(RolDTO.Request.class);
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.actualizarRol(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(rolRepository, never()).existsByNombreRolIgnoreCaseAndIdRolNot(any(), any());
    }

    // ---------- buscarRol ----------
    // Valida que al buscar un rol, el texto de entrada se limpie recortando los espacios en blanco.
    @Test
    void buscarRol_recortaEspaciosDelTexto() {
        when(rolRepository.buscarRolPorNombre("admin")).thenReturn(List.of(nuevoRolMock()));

        List<RolDTO.Response> resultado = rolService.buscarRol("  admin  ");

        assertThat(resultado).hasSize(1);
        verify(rolRepository).buscarRolPorNombre("admin");
    }
    // Comprueba que si la búsqueda por nombre no encuentra coincidencias, se retorna una lista vacía.
    @Test
    void buscarRol_sinCoincidencias_retornaListaVacia() {
        when(rolRepository.buscarRolPorNombre("zzz")).thenReturn(List.of());

        assertThat(rolService.buscarRol("zzz")).isEmpty();
    }

    // ---------- obtenerRol ----------
    // Verifica que al buscar un rol específico por ID y este existe, se retorne la entidad del rol correctamente.
    @Test
    void obtenerRol_cuandoExiste_retornaElRol() {
        Rol rol = nuevoRolMock();
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));

        Rol resultado = rolService.obtenerRol(1L);

        assertThat(resultado).isSameAs(rol);
    }
    // Valida que si se busca un ID de rol que no existe, se lance la excepción RecursoNoEncontradoException.
    @Test
    void obtenerRol_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.obtenerRol(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
