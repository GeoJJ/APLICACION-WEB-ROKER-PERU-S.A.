package RokerPeru.demo.repository;

import RokerPeru.demo.entities.OrdenesServicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenesServicioRepository extends JpaRepository<OrdenesServicio, Long> {
    @EntityGraph(attributePaths = {"servicio", "vendedor", "auxiliar"})
    List<OrdenesServicio> findByCliente_IdUsuarioOrderByFechaOrdenServicioDesc(Long idCliente);

    @EntityGraph(attributePaths = {"cliente", "servicio"})
    List<OrdenesServicio> findByEstadoServicioInOrderByFechaOrdenServicioDesc(List<String> estados);

    boolean existsByIdOrdenServicioAndCliente_IdUsuario(Long idOrdenServicio, Long idCliente);

    // Órdenes que todavía no tienen código (solo se usa al arrancar, ver CodigosInicialesRunner)
    List<OrdenesServicio> findByCodigoOrdenServicioIsNullOrderByIdOrdenServicioAsc();

    // Mayor número de comprobante ya emitido en una serie (null si aún no hay ninguno). Los números son de 8 dígitos con ceros.
    @Query("SELECT MAX(o.numeroComprobanteServicio) FROM OrdenesServicio o WHERE o.serieComprobanteServicio = :serie")
    String ultimoNumeroComprobante(@Param("serie") String serie);

    @EntityGraph(attributePaths = {
            "cliente", "servicio", "vendedor", "auxiliar",
            "asignaciones", "asignaciones.empleado"
    })
    List<OrdenesServicio> findAllByOrderByFechaOrdenServicioDesc();

    @EntityGraph(attributePaths = {
            "cliente", "servicio", "vendedor", "auxiliar",
            "asignaciones", "asignaciones.empleado"
    })
    Optional<OrdenesServicio> findById(Long id);

    // Registro de ventas (reporte PDF): órdenes con comprobante emitido en [desde, hasta), sin canceladas
    @EntityGraph(attributePaths = {"cliente", "servicio", "vendedor"})
    @Query("""
            SELECT o FROM OrdenesServicio o
            WHERE o.numeroComprobanteServicio IS NOT NULL
              AND UPPER(o.estadoServicio) <> 'CANCELADO'
              AND o.fechaEmisionComprobante >= :desde
              AND o.fechaEmisionComprobante < :hasta
            ORDER BY o.fechaEmisionComprobante ASC
            """)
    List<OrdenesServicio> findVentasEmitidasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}