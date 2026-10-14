package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
