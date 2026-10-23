package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Rol;

import java.util.List;

public interface RolRepository extends JpaRepository<Rol, Long> {
    // validar que no se repita el nombre
    boolean existsByNombreRolIgnoreCase(String nombreRol);

    // validar duplicado ignorando el propio registro
    boolean existsByNombreRolIgnoreCaseAndIdRolNot(String nombreRol, Long idRol);

    // buscarRol: "adm" encuentra ADMINISTRADOR
    List<Rol> findByNombreRolContainingIgnoreCaseOrderByNombreRolAsc(String texto);
}
