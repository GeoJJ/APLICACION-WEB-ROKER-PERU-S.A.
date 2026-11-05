package RokerPeru.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("SELECT u FROM Usuario u WHERE u.idUsuario=:id")
 java.util.Optional<Usuario> lockById(@Param("id") Long id);
    @EntityGraph(attributePaths = "rol")
    java.util.Optional<Usuario> findByCorreoUsuarioIgnoreCase(String correo);

    // validar duplicados
    boolean existsByCorreoUsuarioIgnoreCase(String correoUsuario);

    boolean existsByNumeroDocumentoUsuario(String numeroDocumentoUsuario);

    // validar duplicados ignorando el propio registro
    boolean existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(String correoUsuario, Long idUsuario);

    boolean existsByNumeroDocumentoUsuarioAndIdUsuarioNot(String numeroDocumentoUsuario, Long idUsuario);

    // trae el rol en la misma consulta
    @EntityGraph(attributePaths = "rol")
    List<Usuario> findAllByOrderByNombreUsuarioAsc();

    // buscar usuario por nombre, correo o número de documento
    @Query("""
            SELECT u FROM Usuario u JOIN FETCH u.rol
            WHERE LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(u.correoUsuario) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR u.numeroDocumentoUsuario LIKE CONCAT('%', :texto, '%')
            ORDER BY u.nombreUsuario
            """)
    List<Usuario> buscarUsuario(@Param("texto") String texto);

    // eliminar un rol con usuarios no se puede borrar
    boolean existsByRol_IdRol(Long idRol);

    // Obtiene todos los usuarios que tengan un rol específico (ej: "REPARTIDOR")
    // @EntityGraph evita el problema N+1, trayendo el rol en la misma consulta SQL
    @EntityGraph(attributePaths = "rol")
    List<Usuario> findByRol_NombreRolIgnoreCase(String nombreRol);
}
