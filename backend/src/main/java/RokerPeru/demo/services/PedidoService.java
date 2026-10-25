package RokerPeru.demo.services;

import RokerPeru.demo.dto.pedido.PedidoRequestDTO;
import RokerPeru.demo.dto.pedido.PedidoResponseDTO;
import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(PedidoRequestDTO request);
    PedidoResponseDTO confirmarPedido(Long idPedido);
    PedidoResponseDTO cancelarPedido(Long idPedido);
    
    List<PedidoResponseDTO> listarPedidosVendedor();
    List<PedidoResponseDTO> listarPedidosCliente(Long idCliente);
    List<PedidoResponseDTO> listarPedidoRepartidor();
    
    PedidoResponseDTO buscarPedidosVendedor(Long idPedido);
    PedidoResponseDTO buscarPedidosCliente(Long idPedido, Long idCliente);
    PedidoResponseDTO buscarPedidoRepartidor(Long idPedido);
}