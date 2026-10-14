package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    
}
