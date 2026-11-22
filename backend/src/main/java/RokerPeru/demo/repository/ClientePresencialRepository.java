package RokerPeru.demo.repository;

import RokerPeru.demo.entities.ClientePresencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientePresencialRepository extends JpaRepository<ClientePresencial, Long> {
    Optional<ClientePresencial> findByDniClientePresencial(String dniClientePresencial);
}