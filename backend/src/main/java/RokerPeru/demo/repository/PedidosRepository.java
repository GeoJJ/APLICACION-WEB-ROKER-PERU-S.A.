package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Pedidos;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
}
