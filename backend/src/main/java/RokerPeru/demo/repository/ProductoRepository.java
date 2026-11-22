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

    // Productos que todavía no tienen código (solo se usa al arrancar, ver CodigosInicialesRunner)
    @Query("SELECT p FROM Producto p WHERE p.codigoProducto IS NULL OR TRIM(p.codigoProducto) = '' ORDER BY p.idProducto")
    List<Producto> findSinCodigo();

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

    // Registro de inventario (reporte Excel): TODOS los productos con su proveedor ya cargado (evita N+1 consultas)
    @Query("""
            SELECT p FROM Producto p
            LEFT JOIN FETCH p.proveedor
            ORDER BY p.categoriaProducto, p.nombreProducto
            """)
    List<Producto> findAllParaInventario();
}