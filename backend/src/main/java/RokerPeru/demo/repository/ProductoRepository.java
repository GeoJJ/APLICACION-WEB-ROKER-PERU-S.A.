package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoProductoIgnoreCase(String codigo);

    boolean existsByCodigoProductoIgnoreCaseAndIdProductoNot(String codigo, Long id);

    @Query("""
           SELECT p FROM Producto p
           WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
              OR LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
              OR LOWER(p.categoriaProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
           ORDER BY p.nombreProducto
           """)
    List<Producto> buscarProducto(@Param("texto") String texto);
}
