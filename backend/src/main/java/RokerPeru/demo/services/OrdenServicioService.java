package RokerPeru.demo.services;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import java.util.List;

public interface OrdenServicioService {
    
    // Operaciones transaccionales
    OrdenServicioResponseDTO crearOrdenServicio(OrdenServicioRequestDTO request);
    OrdenServicioResponseDTO confirmarOrdenServicio(Long idOrden);
    OrdenServicioResponseDTO cancelarOrdenServicio(Long idOrden);
    
    // Operaciones de listado general según el rol
    List<OrdenServicioResponseDTO> listarOrdenesServicioVendedor();
    List<OrdenServicioResponseDTO> listarOrdenesServicioCliente(Long idCliente);
    List<OrdenServicioResponseDTO> listarOrdenesServicioPersonal();
    
    // Operaciones de búsqueda específica
    OrdenServicioResponseDTO buscarOrdenServicioVendedor(Long idOrden);
    OrdenServicioResponseDTO buscarOrdenServicioCliente(Long idOrden, Long idCliente);
    OrdenServicioResponseDTO buscarOrdenServicioPersonal(Long idOrden);
}