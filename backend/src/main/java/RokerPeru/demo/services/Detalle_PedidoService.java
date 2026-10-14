package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Detalle_Pedido;

public interface Detalle_PedidoService {
    List<Detalle_Pedido> listar();
    Detalle_Pedido guardar(Detalle_Pedido detalle_Pedido);
    void eliminar(Long id);
}
