package RokerPeru.demo.repository;

import RokerPeru.demo.entities.OrdenesServicio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenesServicioRepository extends JpaRepository<OrdenesServicio, Long> {
    @EntityGraph(attributePaths = {"servicio", "vendedor", "auxiliar"})
    List<OrdenesServicio> findByCliente_IdUsuarioOrderByFechaOrdenServicioDesc(Long idCliente);

    @EntityGraph(attributePaths = {"cliente", "servicio"})
    List<OrdenesServicio> findByEstadoServicioInOrderByFechaOrdenServicioDesc(List<String> estados);

    boolean existsByIdOrdenServicioAndCliente_IdUsuario(Long idOrdenServicio, Long idCliente);
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
}