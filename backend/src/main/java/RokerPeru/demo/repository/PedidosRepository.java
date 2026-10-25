package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Pedidos;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
}
