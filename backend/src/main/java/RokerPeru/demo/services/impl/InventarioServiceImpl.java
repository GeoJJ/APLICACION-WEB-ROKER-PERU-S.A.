package RokerPeru.demo.services.impl;

import RokerPeru.demo.services.InventarioService;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.MovimientosProductoRepository;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.MovimientosProducto;
import RokerPeru.demo.dto.movimiento.MovimientoRequestDTO;
import RokerPeru.demo.dto.movimiento.MovimientoResponseDTO;
import RokerPeru.demo.dto.movimiento.AlertaStockDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientosProductoRepository movimientoRepository;

    @Override
    @Transactional
    public MovimientoResponseDTO registrarIngreso(MovimientoRequestDTO request) {
        log.info("Ingreso solicitado para producto ID: {}", request.getProductoId());
        return procesarMovimiento(request, "INGRESO");
    }

    @Override
    @Transactional
    public MovimientoResponseDTO registrarSalida(MovimientoRequestDTO request) {
        log.info("Salida solicitada para producto ID: {}", request.getProductoId());
        return procesarMovimiento(request, "SALIDA");
    }

    @Override
    @Transactional
    public MovimientoResponseDTO registrarAjuste(MovimientoRequestDTO request) {
        log.info("Ajuste solicitado para producto ID: {}", request.getProductoId());
        return procesarMovimiento(request, "AJUSTE");
    }

    // Método privado centralizado para operaciones de escritura
    private MovimientoResponseDTO procesarMovimiento(MovimientoRequestDTO request, String tipo) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int nuevoStock = producto.getStockProducto();

        if (tipo.equals("INGRESO")) {
            nuevoStock += request.getCantidad();
        } else if (tipo.equals("SALIDA") || tipo.equals("AJUSTE")) {
            if (producto.getStockProducto() < request.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para realizar la operación");
            }
            nuevoStock -= request.getCantidad();
        }

        // 1. Actualizar Entidad Producto
        producto.setStockProducto(nuevoStock);
        productoRepository.save(producto);

        // 2. Registrar Historial
        MovimientosProducto movimiento = new MovimientosProducto();
        movimiento.setProducto(producto);
        movimiento.setCantidadMovimientoProducto(request.getCantidad());
        movimiento.setTipoMovimientoProducto(tipo);
        movimiento.setFechaMovimientoProducto(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        // 3. Retornar DTO usando constructor inteligente
        return new MovimientoResponseDTO(movimiento, nuevoStock, tipo + " registrado exitosamente");
    }

    @Override
    public Integer consultarStock(Long idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return producto.getStockProducto();
    }

    @Override
    public List<MovimientoResponseDTO> listarMovimientos() {
        return movimientoRepository.findAll().stream()
                .map(m -> new MovimientoResponseDTO(m, m.getProducto().getStockProducto(), "Historial listado"))
                .toList();
    }

    @Override
    public List<AlertaStockDTO> consultarAlertasStock() {
        // Usa el Query personalizado del repositorio para traer solo los productos en riesgo
        List<Producto> productosEnRiesgo = movimientoRepository.findProductosConBajoStock();
        
        // Mapea al DTO, el cual calcula el nivel de urgencia automáticamente en su constructor
        return productosEnRiesgo.stream()
                .map(AlertaStockDTO::new)
                .toList();
    }
}