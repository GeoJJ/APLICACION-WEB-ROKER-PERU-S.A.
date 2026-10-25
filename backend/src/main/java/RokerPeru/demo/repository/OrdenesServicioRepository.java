package RokerPeru.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import RokerPeru.demo.entities.OrdenesServicio;
import java.util.List;

public interface OrdenesServicioRepository extends JpaRepository<OrdenesServicio, Long> {
    
    // Spring Boot leerá el nombre de este método y automáticamente creará el SQL: 
    // SELECT * FROM ordenes_servicio WHERE id_cliente = ?
    List<OrdenesServicio> findByClienteIdUsuario(Long idCliente);
    
    // Para que el Vendedor pueda ver las órdenes que él mismo gestionó
    List<OrdenesServicio> findByVendedorIdUsuario(Long idVendedor);
    
    // Para que el Personal de Servicio vea solo las órdenes que le importan 
    // (ej. buscar por estados: "CONFIRMADO", "EN_PROCESO")
    List<OrdenesServicio> findByEstadoServicioIn(List<String> estados);
}