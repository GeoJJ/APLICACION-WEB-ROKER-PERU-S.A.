package RokerPeru.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import RokerPeru.demo.entities.Pedidos;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidosRepository extends JpaRepository<Pedidos, Long> {
    boolean existsByIdPedidoAndDelivery_IdUsuario(Long idPedido, Long idDelivery);

    // Pedidos que todavía no tienen código (solo se usa al arrancar, ver CodigosInicialesRunner)
    List<Pedidos> findByCodigoPedidoIsNullOrderByIdPedidoAsc();

    // Mayor número de comprobante ya emitido en una serie (null si aún no hay ninguno). Los números son de 8 dígitos con ceros.
    @Query("SELECT MAX(p.numeroComprobantePedido) FROM Pedidos p WHERE p.serieComprobantePedido = :serie")
    String ultimoNumeroComprobante(@Param("serie") String serie);

    @EntityGraph(attributePaths = {"vendedor", "delivery", "detallePedidos", "detallePedidos.producto"})
    List<Pedidos> findByCliente_IdUsuarioOrderByFechaPedidoDesc(Long idCliente);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "delivery", "detallePedidos", "detallePedidos.producto"})
    List<Pedidos> findByEstadoPedidoOrderByFechaPedidoDesc(String estadoPedido);

    @EntityGraph(attributePaths = {"cliente"})
    List<Pedidos> findByDelivery_IdUsuarioAndEstadoPedidoInOrderByFechaPedidoDesc(Long idDelivery, List<String> estados);

    @EntityGraph(attributePaths = {"cliente", "vendedor", "delivery", "detallePedidos", "detallePedidos.producto"})
    List<Pedidos> findByVendedor_IdUsuarioOrderByFechaPedidoDesc(Long idVendedor);

    // Registro de ventas (reporte PDF): pedidos con comprobante emitido en [desde, hasta), sin cancelados
    @EntityGraph(attributePaths = {"cliente", "clientePresencial", "vendedor"})
    @Query("""
            SELECT p FROM Pedidos p
            WHERE p.numeroComprobantePedido IS NOT NULL
              AND UPPER(p.estadoPedido) <> 'CANCELADO'
              AND p.fechaEmisionComprobante >= :desde
              AND p.fechaEmisionComprobante < :hasta
            ORDER BY p.fechaEmisionComprobante ASC
            """)
    List<Pedidos> findVentasEmitidasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}