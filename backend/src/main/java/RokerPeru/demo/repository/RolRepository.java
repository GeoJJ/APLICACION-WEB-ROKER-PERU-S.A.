package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Rol;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {
    // validar que no se repita el nombre
    boolean existsByNombreRolIgnoreCase(String nombreRol);

    // validar duplicado ignorando el propio registro
    boolean existsByNombreRolIgnoreCaseAndIdRolNot(String nombreRol, Long idRol);

    // buscarRol: "adm" encuentra ADMINISTRADOR
    @Query("SELECT r FROM Rol r " +
            "WHERE LOWER(r.nombreRol) LIKE LOWER(CONCAT('%', :texto, '%')) " +
            "ORDER BY r.nombreRol ASC")
    List<Rol> buscarRolPorNombre(@Param("texto") String texto);}
