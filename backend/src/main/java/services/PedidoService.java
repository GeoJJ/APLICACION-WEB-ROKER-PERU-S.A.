package services;
import entities.Pedidos;
import java.util.List;

public interface PedidoService {
    List<Pedidos> Listar();
    Pedidos guardar(Pedidos pedido);
    void eliminar(Long id);
}
