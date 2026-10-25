package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import RokerPeru.demo.entities.MovimientosProducto;
import RokerPeru.demo.entities.Producto;

public interface MovimientosProductoRepository extends JpaRepository<MovimientosProducto, Long> {
    @Query("SELECT p FROM Producto p WHERE p.stockProducto <= p.stockMinimo")
    List<Producto> findProductosConBajoStock();
}
