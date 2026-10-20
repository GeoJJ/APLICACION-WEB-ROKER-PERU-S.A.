package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.MovimientosProducto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Movimientos_Producto;
import RokerPeru.demo.repository.MovimientosProductoRepository;

@Service 
public class MovimientosProductoService {
    @Autowired
    private MovimientosProductoRepository movimientos_ProductoRepository;

    public List<MovimientosProducto> listar() {
        return movimientos_ProductoRepository.findAll();
    }

    public MovimientosProducto guardar(MovimientosProducto movimientos_Producto) {
        return movimientos_ProductoRepository.save(movimientos_Producto);
    }

    public void eliminar(Long id) {
        movimientos_ProductoRepository.deleteById(id);
    }
}
