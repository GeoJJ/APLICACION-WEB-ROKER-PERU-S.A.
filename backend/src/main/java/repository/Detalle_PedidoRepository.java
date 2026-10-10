package repository;
import entities.Detalle_Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface Detalle_PedidoRepository extends JpaRepository<Detalle_Pedido, Long> {
}
