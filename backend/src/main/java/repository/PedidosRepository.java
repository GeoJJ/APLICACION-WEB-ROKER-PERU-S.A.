package repository;
import entities.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
}
