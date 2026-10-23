package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.EmpleadosServicio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmpleadosServicioRepository extends JpaRepository<EmpleadosServicio, Long> {
    // busca por nombre completo o por cargo
    // "juan pe" encuentra "Juan Pérez"; "técnico" encuentra por cargo
    @Query("""
           SELECT e FROM Empleados_Servicio e
           WHERE LOWER(CONCAT(e.nombreEmpleadoServicio, ' ', e.apellidoEmpleadoServicio))
                 LIKE LOWER(CONCAT('%', :texto, '%'))
              OR LOWER(e.cargoEmpleadoServicio) LIKE LOWER(CONCAT('%', :texto, '%'))
           ORDER BY e.apellidoEmpleadoServicio, e.nombreEmpleadoServicio
           """)
    List<EmpleadosServicio> buscarEmpleadoServicio(@Param("texto") String texto);
}
