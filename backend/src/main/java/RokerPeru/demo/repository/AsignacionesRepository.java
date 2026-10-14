package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Asignaciones;

public interface AsignacionesRepository extends JpaRepository<Asignaciones, Long> {
}
