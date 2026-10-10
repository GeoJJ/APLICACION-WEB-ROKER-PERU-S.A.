package repository;
import entities.Ordenes_Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface Ordenes_ServicioRepository extends JpaRepository<Ordenes_Servicio, Long> {
}
