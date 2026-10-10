package repository;
import entities.Movimientos_Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface Movimeintos_ProductoRepository extends JpaRepository<Movimientos_Producto, Long> {
}
