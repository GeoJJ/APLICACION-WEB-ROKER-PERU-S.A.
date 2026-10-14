package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.repository.ProductoRepository;

public interface ProductoService {
    List<Producto> listar();
    Producto guardar(Producto producto);
    void eliminar(Long id);
}
