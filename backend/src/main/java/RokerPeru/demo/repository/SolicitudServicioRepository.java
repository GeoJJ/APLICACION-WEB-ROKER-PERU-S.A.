package RokerPeru.demo.repository;
import RokerPeru.demo.entities.SolicitudServicio;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicio,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("SELECT s FROM SolicitudServicio s WHERE s.id=:id")
 Optional<SolicitudServicio> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
