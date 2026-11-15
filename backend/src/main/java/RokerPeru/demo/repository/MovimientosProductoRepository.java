package RokerPeru.demo.repository;

import RokerPeru.demo.entities.MovimientosProducto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientosProductoRepository extends JpaRepository<MovimientosProducto, Long> {

    // listarMovimientos: más recientes primero, con el producto en la misma consulta (evita N+1)
    @EntityGraph(attributePaths = "producto")
    List<MovimientosProducto> findAllByOrderByFechaMovimientoProductoDesc();

}