package repository;
import entities.Asignaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AsignacionesRepository extends JpaRepository<Asignaciones, Long> {
}
