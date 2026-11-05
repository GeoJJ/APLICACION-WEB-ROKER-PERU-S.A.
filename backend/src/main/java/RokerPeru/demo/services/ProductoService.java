package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedoresRepository proveedoresRepository;

    @Transactional
    public ProductoDTO.Response crearProducto(ProductoDTO.Request dto) {
        if (productoRepository.existeCodigoDuplicado(dto.codigoProducto(), null)) {
            throw new RuntimeException("Ya existe un producto con el código: " + dto.codigoProducto());
        }

        Producto guardado = productoRepository.save(dto.toEntity(obtenerProveedor(dto.idProveedor())));
        log.info("Producto creado: id={}, codigo={}", guardado.getIdProducto(), dto.codigoProducto());

        return ProductoDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO.Response> listarProducto() {
        return productoRepository.findAll(Sort.by("nombreProducto"))
                .stream().map(ProductoDTO.Response::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO.Response buscarPorIdProducto(Long id) {
        return ProductoDTO.Response.fromEntity(obtenerProducto(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO.Response> buscarProducto(String texto) {
        return productoRepository.buscarProducto(texto.trim())
                .stream().map(ProductoDTO.Response::fromEntity).toList();
    }

    @Transactional
    public ProductoDTO.Response actualizarProducto(Long id, ProductoDTO.Request dto) {
        Producto producto = obtenerProducto(id);

        if (productoRepository.existeCodigoDuplicado(dto.codigoProducto(), id)) {
            throw new RuntimeException("Ya existe un producto con el código: " + dto.codigoProducto());
        }

        dto.applyTo(producto, obtenerProveedor(dto.idProveedor()));
        log.info("Producto actualizado: id={}, codigo={}", id, dto.codigoProducto());

        return ProductoDTO.Response.fromEntity(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = obtenerProducto(id);
        productoRepository.delete(producto);
        log.info("Producto eliminado: id={}, codigo={}", id, producto.getCodigoProducto());
    }

    @Transactional
    public ProductoDTO.Response cambiarEstadoProducto(Long id, String estado) {
        String nuevoEstado = estado != null ? estado.trim().toUpperCase() : null;
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new RuntimeException("El estado es obligatorio");
        }

        Producto producto = obtenerProducto(id);
        producto.setEstadoProducto(nuevoEstado);
        log.info("Estado de producto id={} cambió a {}", id, nuevoEstado);

        return ProductoDTO.Response.fromEntity(producto);
    }

    // Métodos específicos para la Vista del Cliente
    @Transactional(readOnly = true)
    public List<ProductoDTO.ClienteResponse> listarProductosCliente() {
        return productoRepository.findAll(Sort.by("nombreProducto"))
                .stream().map(ProductoDTO.ClienteResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDTO.ClienteResponse obtenerProductoCliente(Long id) {
        return ProductoDTO.ClienteResponse.fromEntity(obtenerProducto(id));
    }

    //Metodos privados
    private Proveedores obtenerProveedor(Long idProveedor) {
        return proveedoresRepository.findById(idProveedor)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + idProveedor));
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }
}