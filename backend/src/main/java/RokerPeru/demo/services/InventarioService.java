package RokerPeru.demo.services;

import RokerPeru.demo.dto.MovimientoDTO;
import RokerPeru.demo.entities.MovimientosProducto;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.repository.MovimientosProductoRepository;
import RokerPeru.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientosProductoRepository movimientoRepository;

    @Transactional
    public MovimientoDTO.Response registrarIngreso(MovimientoDTO.Request request) {
        return procesarMovimiento(request, "INGRESO");
    }

    @Transactional
    public MovimientoDTO.Response registrarSalida(MovimientoDTO.Request request) {
        return procesarMovimiento(request, "SALIDA");
    }

    @Transactional
    public MovimientoDTO.Response registrarAjuste(MovimientoDTO.Request request) {
        return procesarMovimiento(request, "AJUSTE");
    }

    // Método privado centralizado para operaciones de escritura
    private MovimientoDTO.Response procesarMovimiento(MovimientoDTO.Request request, String tipo) {
        // Con bloqueo de fila: dos peticiones simultáneas sobre el mismo producto se procesan en fila
        Producto producto = productoRepository.buscarParaActualizarStock(request.productoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + request.productoId()));

        int stockAnterior = producto.getStockProducto();
        int nuevoStock = tipo.equals("INGRESO")
                ? stockAnterior + request.cantidad()
                : stockAnterior - request.cantidad();

        if (nuevoStock < 0) {
            log.warn("{} rechazado, producto id={}: stock insuficiente (disponible={}, solicitado={})",
                    tipo, producto.getIdProducto(), stockAnterior, request.cantidad());
            throw new RuntimeException("Stock insuficiente. Disponible: " + stockAnterior);
        }

        producto.setStockProducto(nuevoStock);

        MovimientosProducto movimiento = new MovimientosProducto();
        movimiento.setProducto(producto);
        movimiento.setCantidadMovimientoProducto(request.cantidad());
        movimiento.setTipoMovimientoProducto(tipo);
        movimiento.setFechaMovimientoProducto(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        log.info("{} registrado: producto id={}, cantidad={}, stock {} -> {}",
                tipo, producto.getIdProducto(), request.cantidad(), stockAnterior, nuevoStock);

        return MovimientoDTO.Response.fromEntity(movimiento, nuevoStock, tipo + " registrado exitosamente");
    }

    public Integer consultarStock(Long idProducto) {
        return productoRepository.consultarStockPorId(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));
    }

    public List<MovimientoDTO.Response> listarMovimientos() {
        return movimientoRepository.findAllByOrderByFechaMovimientoProductoDesc().stream()
                .map(m -> MovimientoDTO.Response.fromEntity(m, m.getProducto().getStockProducto(), "Historial listado"))
                .toList();
    }

    public List<MovimientoDTO.AlertaStockResponse> consultarAlertasStock() {
        // La consulta vive en ProductoRepository (devuelve productos); el DTO calcula la urgencia
        return productoRepository.findProductosConBajoStock().stream()
                .map(MovimientoDTO.AlertaStockResponse::fromEntity)
                .toList();
    }
}