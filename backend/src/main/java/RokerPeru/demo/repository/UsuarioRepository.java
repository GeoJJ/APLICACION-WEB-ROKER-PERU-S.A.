package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

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
}
