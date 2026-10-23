package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Proveedores;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProveedoresRepository extends JpaRepository<Proveedores, Long> {
    // el RUC es único, validar antes de guardar
    boolean existsByRucProveedor(String rucProveedor);

    // validar duplicado ignorando el propio registro
    boolean existsByRucProveedorAndIdProveedorNot(String rucProveedor, Long idProveedor);

    // buscar un proveedor por el nombre
    @Query("""
           SELECT p FROM Proveedores p
           WHERE LOWER(p.nombreProveedor) LIKE LOWER(CONCAT('%', :texto, '%'))
              OR p.rucProveedor LIKE CONCAT('%', :texto, '%')
           ORDER BY p.nombreProveedor
           """)
    List<Proveedores> buscar(@Param("texto") String texto);
}
