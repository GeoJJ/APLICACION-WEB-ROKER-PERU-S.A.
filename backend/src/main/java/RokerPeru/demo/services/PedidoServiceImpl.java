package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.repository.PedidosRepository;

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
