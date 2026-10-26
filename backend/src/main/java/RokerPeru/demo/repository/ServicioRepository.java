package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.Servicio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    @Query("""
       SELECT s FROM Servicio s
       WHERE LOWER(s.nombreServicio) LIKE LOWER(CONCAT('%', :texto, '%'))
          OR LOWER(s.categoriaServicio) LIKE LOWER(CONCAT('%', :texto, '%'))
          OR LOWER(s.tipoServicio) LIKE LOWER(CONCAT('%', :texto, '%'))
       ORDER BY s.nombreServicio
       """)
    List<Servicio> buscarServicio(@Param("texto") String texto);
}
