package RokerPeru.demo.controller;
import RokerPeru.demo.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/public") @RequiredArgsConstructor @Transactional(readOnly=true)
public class PublicCatalogController {
 private final ProductoRepository products; private final ServicioRepository services;
 public record Product(Long id,String name,String category,double price,int stock) {}
 public record Service(Long id,String name,String category,String description) {}
 @GetMapping("/products") public List<Product> products(){return products.findAll().stream().filter(p -> !"INACTIVO".equalsIgnoreCase(p.getEstadoProducto())).map(p -> new Product(p.getIdProducto(),p.getNombreProducto(),p.getCategoriaProducto(),p.getPrecioProducto(),p.getStockProducto())).toList();}
 @GetMapping("/services") public List<Service> services(){return services.findAll().stream().filter(s -> s.isActivo()).map(s -> new Service(s.getIdServicio(),s.getNombreServicio(),s.getCategoriaServicio(),s.getDescripcionServicio())).toList();}
}
