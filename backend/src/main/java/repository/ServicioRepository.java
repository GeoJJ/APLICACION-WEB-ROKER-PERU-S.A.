package repository;
import entities.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}
