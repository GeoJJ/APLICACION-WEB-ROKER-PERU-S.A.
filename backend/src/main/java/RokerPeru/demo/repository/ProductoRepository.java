package RokerPeru.demo.repository;

import RokerPeru.demo.entities.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // crearProducto / actualizarProducto valida código duplicado (idActual = null al crear)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Producto p " +
            "WHERE LOWER(p.codigoProducto) = LOWER(:codigo) " +
            "AND (:idActual IS NULL OR p.idProducto != :idActual)")
    boolean existeCodigoDuplicado(@Param("codigo") String codigo, @Param("idActual") Long idActual);

    // buscarProducto por nombre, código o categoría
    @Query("""
            SELECT p FROM Producto p
            WHERE LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(p.codigoProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(p.categoriaProducto) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY p.nombreProducto
            """)
    List<Producto> buscarProducto(@Param("texto") String texto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> buscarParaActualizarStock(@Param("id") Long id);

    // consultarStock, trae solo la columna, no la entidad completa
    @Query("SELECT p.stockProducto FROM Producto p WHERE p.idProducto = :id")
    Optional<Integer> consultarStockPorId(@Param("id") Long id);

    // consultarAlertasStock, más escasos primero y sin productos dados de baja
    @Query("""
            SELECT p FROM Producto p
            WHERE p.stockProducto <= p.stockMinimo
              AND (p.estadoProducto IS NULL OR UPPER(p.estadoProducto) <> 'INACTIVO')
            ORDER BY p.stockProducto ASC, p.nombreProducto ASC
            """)
    List<Producto> findProductosConBajoStock();
}