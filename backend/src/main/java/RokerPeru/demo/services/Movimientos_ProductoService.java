package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Movimientos_Producto;

public interface Movimientos_ProductoService {
    List<Movimientos_Producto> listar();
    Movimientos_Producto guardar(Movimientos_Producto movimientos_Producto);
    void eliminar(Long id);
}
