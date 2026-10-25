package RokerPeru.demo.services;

import java.util.List;

import RokerPeru.demo.dto.movimiento.AlertaStockDTO;
import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;

public interface InventarioService {
    // Recibe los datos de entrada y devuelve el recibo de la operación
    MovimientoResponseDTO registrarIngreso(MovimientoRequestDTO request);
    MovimientoResponseDTO registrarSalida(MovimientoRequestDTO request);
    MovimientoResponseDTO registrarAjuste(MovimientoRequestDTO request);
    Integer consultarStock(Long productoId);
    List<MovimientoResponseDTO> listarMovimientos();
    List<AlertaStockDTO> consultarAlertasStock();
}