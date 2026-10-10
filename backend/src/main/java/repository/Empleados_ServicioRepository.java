package repository;
import entities.Empleados_Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface Empleados_ServicioRepository extends JpaRepository<Empleados_Servicio, Long> {
}
