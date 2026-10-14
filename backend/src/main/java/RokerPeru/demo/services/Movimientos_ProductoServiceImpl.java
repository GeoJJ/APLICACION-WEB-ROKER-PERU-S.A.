package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Movimientos_Producto;
import RokerPeru.demo.repository.Movimientos_ProductoRepository;

@Service 
public class Movimientos_ProductoServiceImpl implements Movimientos_ProductoService{
    @Autowired
    private Movimientos_ProductoRepository movimientos_ProductoRepository;

    @Override
    public List<Movimientos_Producto> listar() {
        return movimientos_ProductoRepository.findAll();
    }

    @Override
    public Movimientos_Producto guardar(Movimientos_Producto movimientos_Producto) {
        return movimientos_ProductoRepository.save(movimientos_Producto);
    }

    @Override
    public void eliminar(Long id) {
        movimientos_ProductoRepository.deleteById(id);
    }
}
