package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
