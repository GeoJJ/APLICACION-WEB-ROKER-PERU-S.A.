package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Empleados_Servicio;

public interface Empleados_ServicioRepository extends JpaRepository<Empleados_Servicio, Long> {
}
