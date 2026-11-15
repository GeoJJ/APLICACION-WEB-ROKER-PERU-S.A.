package RokerPeru.demo.repository;

import RokerPeru.demo.entities.Asignaciones;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionesRepository extends JpaRepository<Asignaciones, Long> {
    @EntityGraph(attributePaths = {"empleado", "ordenServicio"})
    List<Asignaciones> findByOrdenServicio_IdOrdenServicioOrderByFechaAsignacionDesc(Long idOrdenServicio);

    @EntityGraph(attributePaths = {"empleado", "ordenServicio.cliente"})
    List<Asignaciones> findByEmpleado_IdEmpleadoServicioOrderByFechaAsignacionDesc(Long idEmpleado);

    @EntityGraph(attributePaths = {"empleado", "ordenServicio"})
    List<Asignaciones> findAllByOrderByFechaAsignacionDesc();

    boolean existsByOrdenServicio_IdOrdenServicioAndEmpleado_IdEmpleadoServicio(Long idOrden, Long idEmpleado);
}