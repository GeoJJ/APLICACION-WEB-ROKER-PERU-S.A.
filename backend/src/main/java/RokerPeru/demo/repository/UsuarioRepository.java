package RokerPeru.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // --- MÉTODO NUEVO PARA EL LOGIN ---
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);

    // --- MÉTODOS ANTERIORES ---
    boolean existsByCorreoUsuarioIgnoreCase(String correoUsuario);

    boolean existsByNumeroDocumentoUsuario(String numeroDocumentoUsuario);

    boolean existsByCorreoUsuarioIgnoreCaseAndIdUsuarioNot(String correoUsuario, Long idUsuario);

    boolean existsByNumeroDocumentoUsuarioAndIdUsuarioNot(String numeroDocumentoUsuario, Long idUsuario);

    boolean existsByRol_IdRol(Long idRol);

    @EntityGraph(attributePaths = "rol")
    List<Usuario> findByRol_NombreRolIgnoreCase(String nombreRol);

    // Solo lista los usuarios activos
    @EntityGraph(attributePaths = "rol")
    List<Usuario> findByActivoTrueOrderByNombreUsuarioAsc();

    // Solo repartidores activos, para que no se asigne uno desactivado
    @EntityGraph(attributePaths = "rol")
    List<Usuario> findByRol_NombreRolIgnoreCaseAndActivoTrue(String nombreRol);

    // Lista a todos los usuarios, activos y desactivados
    @EntityGraph(attributePaths = "rol")
    List<Usuario> findAllByOrderByNombreUsuarioAsc();

    // Busca entre todos los usuarios
    @Query("""
        SELECT u FROM Usuario u JOIN FETCH u.rol
        WHERE LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR LOWER(u.correoUsuario) LIKE LOWER(CONCAT('%', :texto, '%'))
           OR u.numeroDocumentoUsuario LIKE CONCAT('%', :texto, '%')
        ORDER BY u.nombreUsuario
        """)
    List<Usuario> buscarUsuario(@Param("texto") String texto);
}