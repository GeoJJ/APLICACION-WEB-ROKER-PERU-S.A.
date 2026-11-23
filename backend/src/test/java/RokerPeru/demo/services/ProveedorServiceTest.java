package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
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
class ProveedorServiceTest {

    @Mock
    private ProveedoresRepository proveedoresRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private static final String RUC = "20123456789";

    private Proveedores proveedorMock() {
        return mock(Proveedores.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearProveedor ----------

    @Test
    void crearProveedor_conRucNuevo_guardaElProveedor() {
        ProveedorDTO.Request dto = mock(ProveedorDTO.Request.class);
        Proveedores proveedor = proveedorMock();

        when(dto.rucProveedor()).thenReturn(RUC);
        when(proveedoresRepository.existsByRucProveedor(RUC)).thenReturn(false);
        when(dto.toEntity()).thenReturn(proveedor);
        when(proveedoresRepository.save(proveedor)).thenReturn(proveedor);

        ProveedorDTO.Response response = proveedorService.crearProveedor(dto);

        assertThat(response).isNotNull();
        verify(proveedoresRepository).save(proveedor);
    }

    @Test
    void crearProveedor_conRucDuplicado_lanzaConflictoYNoGuarda() {
        ProveedorDTO.Request dto = mock(ProveedorDTO.Request.class);
        when(dto.rucProveedor()).thenReturn(RUC);
        when(proveedoresRepository.existsByRucProveedor(RUC)).thenReturn(true);

        assertThatThrownBy(() -> proveedorService.crearProveedor(dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(RUC);

        verify(proveedoresRepository, never()).save(any());
    }

    // ---------- listar / buscar ----------

    @Test
    void listarProveedor_retornaProveedoresOrdenadosPorNombre() {
        when(proveedoresRepository.findAll(Sort.by("nombreProveedor")))
                .thenReturn(List.of(proveedorMock(), proveedorMock()));

        List<ProveedorDTO.Response> resultado = proveedorService.listarProveedor();

        assertThat(resultado).hasSize(2);
        verify(proveedoresRepository).findAll(Sort.by("nombreProveedor"));
    }

    @Test
    void listarProveedor_sinProveedores_retornaListaVacia() {
        when(proveedoresRepository.findAll(Sort.by("nombreProveedor"))).thenReturn(List.of());

        assertThat(proveedorService.listarProveedor()).isEmpty();
    }

    @Test
    void buscarProveedor_recortaEspaciosDelTexto() {
        when(proveedoresRepository.buscarProveedor("acme")).thenReturn(List.of(proveedorMock()));

        List<ProveedorDTO.Response> resultado = proveedorService.buscarProveedor("  acme  ");

        assertThat(resultado).hasSize(1);
        verify(proveedoresRepository).buscarProveedor("acme");
    }

    @Test
    void buscarProveedor_sinCoincidencias_retornaListaVacia() {
        when(proveedoresRepository.buscarProveedor("zzz")).thenReturn(List.of());

        assertThat(proveedorService.buscarProveedor("zzz")).isEmpty();
    }

    // ---------- actualizarProveedor ----------

    @Test
    void actualizarProveedor_conDatosValidos_aplicaCambios() {
        ProveedorDTO.Request dto = mock(ProveedorDTO.Request.class);
        Proveedores proveedor = proveedorMock();

        when(proveedoresRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(dto.rucProveedor()).thenReturn(RUC);
        when(proveedoresRepository.existsByRucProveedorAndIdProveedorNot(RUC, 1L)).thenReturn(false);

        ProveedorDTO.Response response = proveedorService.actualizarProveedor(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(proveedor);
    }

    @Test
    void actualizarProveedor_conRucDeOtroProveedor_lanzaConflicto() {
        ProveedorDTO.Request dto = mock(ProveedorDTO.Request.class);

        when(proveedoresRepository.findById(1L)).thenReturn(Optional.of(proveedorMock()));
        when(dto.rucProveedor()).thenReturn(RUC);
        when(proveedoresRepository.existsByRucProveedorAndIdProveedorNot(RUC, 1L)).thenReturn(true);

        assertThatThrownBy(() -> proveedorService.actualizarProveedor(1L, dto))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining(RUC);

        verify(dto, never()).applyTo(any());
    }

    @Test
    void actualizarProveedor_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        ProveedorDTO.Request dto = mock(ProveedorDTO.Request.class);
        when(proveedoresRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> proveedorService.actualizarProveedor(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(proveedoresRepository, never()).existsByRucProveedorAndIdProveedorNot(any(), any());
    }

    // ---------- eliminarProveedor ----------

    @Test
    void eliminarProveedor_cuandoExiste_loElimina() {
        Proveedores proveedor = proveedorMock();
        when(proveedoresRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        proveedorService.eliminarProveedor(1L);

        verify(proveedoresRepository).delete(proveedor);
    }

    @Test
    void eliminarProveedor_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(proveedoresRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> proveedorService.eliminarProveedor(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(proveedoresRepository, never()).delete(any());
    }
}