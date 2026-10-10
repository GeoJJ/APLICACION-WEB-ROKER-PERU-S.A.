package repository;
import entities.Proveedores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ProveedoresRepository extends JpaRepository<Proveedores, Long> {
}
