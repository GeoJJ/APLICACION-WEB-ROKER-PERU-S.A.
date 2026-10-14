package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Ordenes_Servicio;

public interface Ordenes_ServicioRepository extends JpaRepository<Ordenes_Servicio, Long> {
}
