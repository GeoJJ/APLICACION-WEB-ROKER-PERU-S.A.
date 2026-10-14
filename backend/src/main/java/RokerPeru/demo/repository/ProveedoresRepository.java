package RokerPeru.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import RokerPeru.demo.entities.Proveedores;

public interface ProveedoresRepository extends JpaRepository<Proveedores, Long> {
}
