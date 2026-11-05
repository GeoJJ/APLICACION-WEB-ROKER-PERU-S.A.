package RokerPeru.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Pedidos;

import java.util.List;
import java.util.Optional;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
 Optional<Pedidos> findByCheckoutKey(String checkoutKey);
 Optional<Pedidos> findByNumeroComprobantePedido(String numero);
    boolean existsByIdPedidoAndDelivery_IdUsuario(Long idPedido, Long idUsuario);

    @EntityGraph(attributePaths = {"vendedor", "delivery", "detallePedidos", "detallePedidos.producto"})
    List<Pedidos> findByCliente_IdUsuarioOrderByFechaPedidoDesc(Long idCliente);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "delivery", "detallePedidos", "detallePedidos.producto"})
    List<Pedidos> findByEstadoPedidoOrderByFechaPedidoDesc(String estadoPedido);

    @EntityGraph(attributePaths = {"cliente"})
    List<Pedidos> findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(Long idDelivery, List<String> estados);
}
