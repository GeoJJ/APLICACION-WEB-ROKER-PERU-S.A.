package RokerPeru.demo.repository;

import RokerPeru.demo.entities.SecuenciaComprobante;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SecuenciaComprobanteRepository extends JpaRepository<SecuenciaComprobante, Long> {

    Optional<SecuenciaComprobante> findBySerie(String serie);

    // Bloquea la fila mientras dura la transacción: nadie más toma un número de esta serie
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaComprobante s WHERE s.serie = :serie")
    Optional<SecuenciaComprobante> findBySerieParaActualizar(@Param("serie") String serie);
}