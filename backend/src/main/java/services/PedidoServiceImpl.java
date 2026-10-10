package services;
import entities.Pedidos;
import java.util.List;
import repository.PedidosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoServiceImpl implements PedidoService {
    @Autowired
    private PedidosRepository pedidosRepository;

    @Override
    public List<Pedidos> Listar() {
        return pedidosRepository.findAll();
    }

    @Override
    public Pedidos guardar(Pedidos pedido) {
        return pedidosRepository.save(pedido);
    }

    @Override
    public void eliminar(Long id) {
        pedidosRepository.deleteById(id);
    }
}
