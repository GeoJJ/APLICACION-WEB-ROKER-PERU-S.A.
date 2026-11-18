package RokerPeru.demo.repository;

import RokerPeru.demo.entities.TokenInvalido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TokenInvalidoRepository extends JpaRepository<TokenInvalido, Long> {
    Optional<TokenInvalido> findByToken(String token);
}
