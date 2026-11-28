package RokerPeru.demo.services;

import RokerPeru.demo.dto.UsuarioDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolService rolService;

    @InjectMocks
    private UsuarioService usuarioService;

    private static final String CORREO = "usuario@roker.com";
    private static final String DOCUMENTO = "12345678";

    private Usuario nuevoUsuarioMock() {
        return mock(Usuario.class, RETURNS_DEEP_STUBS);
    }

    // ---------- crearUsuario ----------
    // Verifica que al crear un usuario con datos válidos se encripta la contraseña y se guarda correctamente.
    @Test
    void crearUsuario_conDatosValidos_guardaConPasswordEncriptado() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        Rol rol = mock(Rol.class);
        Usuario usuario = nuevoUsuarioMock();

        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(dto.passwordUsuario()).thenReturn("123456");
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCase(CORREO)).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumentoUsuario(DOCUMENTO)).thenReturn(false);
        when(rolService.obtenerRol(any())).thenReturn(rol);
        when(dto.toEntity(rol)).thenReturn(usuario);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioDTO.Response response = usuarioService.crearUsuario(dto);

        assertThat(response).isNotNull();
        verify(usuario).setPasswordUsuario("hash");
        verify(usuarioRepository).save(usuario);
    }
    // Comprueba que si el correo ya existe, se lanza una excepción y no se guarda el usuario.
    @Test
    void crearUsuario_conCorreoDuplicado_lanzaExcepcionYNoGuarda() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        when(dto.passwordUsuario()).thenReturn("12345678");
        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCase(CORREO)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(CORREO);

        verify(usuarioRepository, never()).save(any());
    }
    // Valida que si el número de documento ya está registrado, se lanza una excepción y no se guarda.
    @Test
    void crearUsuario_conDocumentoDuplicado_lanzaExcepcionYNoGuarda() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        when(dto.passwordUsuario()).thenReturn("12345678");
        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCase(CORREO)).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumentoUsuario(DOCUMENTO)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crearUsuario(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(DOCUMENTO);

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- listar / buscar ----------
    // Verifica que se retorne la lista de usuarios activos ordenada por nombre.
    @Test
    void listarUsuarios_retornaTodosLosUsuarios() {
        when(usuarioRepository.findAllByOrderByNombreUsuarioAsc())
                .thenReturn(List.of(nuevoUsuarioMock(), nuevoUsuarioMock()));

        List<UsuarioDTO.Response> resultado = usuarioService.listarUsuarios();

        assertThat(resultado).hasSize(2);
    }
    // Comprueba que si no existen usuarios activos, se retorna una lista vacía.
    @Test
    void listarUsuarios_cuandoNoHayUsuarios_retornaListaVacia() {
        when(usuarioRepository.findAllByOrderByNombreUsuarioAsc()).thenReturn(List.of());

        assertThat(usuarioService.listarUsuarios()).isEmpty();
    }
    // Valida que al buscar un usuario se recorten los espacios en blanco del texto de entrada.
    @Test
    void buscarUsuario_recortaEspaciosDelTexto() {
        when(usuarioRepository.buscarUsuario("juan")).thenReturn(List.of(nuevoUsuarioMock()));

        List<UsuarioDTO.Response> resultado = usuarioService.buscarUsuario("  juan  ");

        assertThat(resultado).hasSize(1);
        verify(usuarioRepository).buscarUsuario("juan");
    }

    // ---------- actualizarUsuario ----------
    // Verifica que al actualizar con una nueva contraseña, esta se encripta y se aplica al usuario.
    @Test
    void actualizarUsuario_conPassword_encriptaYActualiza() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        Usuario usuario = nuevoUsuarioMock();
        Rol rol = mock(Rol.class);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(dto.passwordUsuario()).thenReturn("12345678");
        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(dto.passwordUsuario()).thenReturn("nueva123");
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(CORREO, 1L)).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumentoUsuarioAndIdUsuarioNot(DOCUMENTO, 1L)).thenReturn(false);
        when(rolService.obtenerRol(any())).thenReturn(rol);
        when(passwordEncoder.encode("nueva123")).thenReturn("hashNuevo");

        UsuarioDTO.Response response = usuarioService.actualizarUsuario(1L, dto);

        assertThat(response).isNotNull();
        verify(dto).applyTo(usuario, rol);
        verify(usuario).setPasswordUsuario("hashNuevo");
    }
    // Comprueba que si la contraseña enviada está vacía, no se modifica la contraseña actual del usuario.
    @Test
    void actualizarUsuario_conPasswordVacio_noCambiaElPassword() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        Usuario usuario = nuevoUsuarioMock();
        Rol rol = mock(Rol.class);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(dto.passwordUsuario()).thenReturn("12345678");
        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(dto.passwordUsuario()).thenReturn("   ");
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(CORREO, 1L)).thenReturn(false);
        when(usuarioRepository.existsByNumeroDocumentoUsuarioAndIdUsuarioNot(DOCUMENTO, 1L)).thenReturn(false);
        when(rolService.obtenerRol(any())).thenReturn(rol);

        usuarioService.actualizarUsuario(1L, dto);

        verify(passwordEncoder, never()).encode(any());
        verify(usuario, never()).setPasswordUsuario(any());
    }
    // Valida que si el ID del usuario a actualizar no existe, se lanza RecursoNoEncontradoException.
    @Test
    void actualizarUsuario_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizarUsuario(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(usuarioRepository, never()).save(any());
    }
    // Comprueba que si se intenta usar el correo de otro usuario al actualizar, se lanza una excepción.
    @Test
    void actualizarUsuario_conCorreoDeOtroUsuario_lanzaExcepcion() {
        UsuarioDTO.Request dto = mock(UsuarioDTO.Request.class);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(nuevoUsuarioMock()));
        when(dto.correoUsuario()).thenReturn(CORREO);
        when(dto.numeroDocumentoUsuario()).thenReturn(DOCUMENTO);
        when(usuarioRepository.existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(CORREO, 1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizarUsuario(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(CORREO);

        verify(rolService, never()).obtenerRol(any());
    }

    // ---------- eliminarUsuario ----------
    // Verifica que al eliminar un usuario existente solo se desactiva y no se borra de la base de datos.
    @Test
    void eliminarUsuario_cuandoExiste_loDesactivaSinBorrarlo() {
        Usuario usuario = nuevoUsuarioMock();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(1L);

        // Queda desactivado y con la sesión invalidada (la versión sube de 0 a 1)
        verify(usuario).setActivo(false);
        verify(usuario).setVersionSesion(1);
        verify(usuarioRepository).save(usuario);
        verify(usuarioRepository, never()).delete(any());
    }
    // Valida que al desactivar un ID inexistente se lance RecursoNoEncontradoException y no se guarde nada.
    @Test
    void eliminarUsuario_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    // ---------- repartidores ----------
    // Comprueba que se consulten correctamente los usuarios activos que tienen el rol de repartidor.
    @Test
    void listarRepartidoresDisponibles_consultaPorRolRepartidor() {
        when(usuarioRepository.findByRol_NombreRolIgnoreCaseAndActivoTrue("REPARTIDOR"))
                .thenReturn(List.of(nuevoUsuarioMock()));

        List<UsuarioDTO.Response> resultado = usuarioService.listarRepartidoresDisponibles();

        assertThat(resultado).hasSize(1);
        verify(usuarioRepository).findByRol_NombreRolIgnoreCaseAndActivoTrue("REPARTIDOR");
    }
    // Verifica que al reactivar un usuario existente queda activo de nuevo
    @Test
    void reactivarUsuario_cuandoExiste_loDejaActivo() {
        Usuario usuario = nuevoUsuarioMock();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.reactivarUsuario(1L);

        verify(usuario).setActivo(true);
        verify(usuarioRepository).save(usuario);
    }
}