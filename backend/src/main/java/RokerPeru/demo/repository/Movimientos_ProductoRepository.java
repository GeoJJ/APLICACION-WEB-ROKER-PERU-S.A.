package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Movimientos_Producto;

public interface Movimientos_ProductoRepository extends JpaRepository<Movimientos_Producto, Long> {
}
