package RokerPeru.demo.repository;

import RokerPeru.demo.entities.SecuenciaComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SecuenciaComprobanteRepository extends JpaRepository<SecuenciaComprobante, Long> {

    Optional<SecuenciaComprobante> findBySerie(String serie);

    // Suma 1 directamente en la base de datos (seguro contra duplicados)
    @Modifying
    @Query("UPDATE SecuenciaComprobante s SET s.ultimoNumero = s.ultimoNumero + 1 WHERE s.serie = :serie")
    void incrementarNumero(@Param("serie") String serie);
}