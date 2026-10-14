package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Detalle_Pedido;
import RokerPeru.demo.repository.Detalle_PedidoRepository;

@Service 
public class Detalle_PedidoServiceImpl implements Detalle_PedidoService{

    @Autowired
    private Detalle_PedidoRepository detalle_PedidoRepository;

    @Override
    public List<Detalle_Pedido> listar() {
        return detalle_PedidoRepository.findAll();
    }

    @Override
    public Detalle_Pedido guardar(Detalle_Pedido detalle_Pedido) {
        return detalle_PedidoRepository.save(detalle_Pedido);
    }

    @Override
    public void eliminar(Long id) {
        detalle_PedidoRepository.deleteById(id);
    }

}
