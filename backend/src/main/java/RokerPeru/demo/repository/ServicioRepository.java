package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
}
