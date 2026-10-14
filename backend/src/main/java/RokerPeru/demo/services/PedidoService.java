package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Pedidos;

public interface PedidoService {
    List<Pedidos> Listar();
    Pedidos guardar(Pedidos pedido);
    void eliminar(Long id);
}
