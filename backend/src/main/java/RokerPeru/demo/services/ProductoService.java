package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProductoClienteResponseDTO;
import RokerPeru.demo.dto.ProductoRequestDTO;
import RokerPeru.demo.dto.ProductoResponseDTO;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.extern.slf4j.Slf4j;   // ← nuevo import

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedoresRepository proveedoresRepository;

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        String codigo = normalizar(dto.getCodigoProducto());
        validarCodigoDuplicado(codigo, null);

        Producto p = new Producto();
        copiarDatos(p, dto, codigo);
        Producto guardado = productoRepository.save(p);

        log.info("Producto creado: id={}, codigo={}", guardado.getIdProducto(), codigo);
        return aDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProducto() {
        List<ProductoResponseDTO> productos = productoRepository.findAll(Sort.by("nombreProducto"))
                .stream()
                .map(this::aDto)
                .toList();

        log.debug("Listado de productos: {} resultados", productos.size());
        return productos;
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorIdProducto(Long id) {
        return aDto(obtenerProducto(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarProducto(String texto) {
        List<ProductoResponseDTO> productos = productoRepository.buscarProducto(texto.trim())
                .stream()
                .map(this::aDto)
                .toList();

        log.debug("Búsqueda de productos con texto='{}': {} resultados", texto.trim(), productos.size());
        return productos;
    }

    @Transactional
    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO dto) {
        Producto p = obtenerProducto(id);
        String codigo = normalizar(dto.getCodigoProducto());
        validarCodigoDuplicado(codigo, id);

        copiarDatos(p, dto, codigo);

        log.info("Producto actualizado: id={}, codigo={}", id, codigo);
        return aDto(p);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        Producto p = obtenerProducto(id);
        productoRepository.delete(p);

        log.info("Producto eliminado: id={}, codigo={}", id, p.getCodigoProducto());
    }

    @Transactional
    public ProductoResponseDTO cambiarEstadoProducto(Long id, String estado) {
        String nuevoEstado = normalizar(estado);
        if (nuevoEstado == null) {
            log.warn("Cambio de estado rechazado para producto id={}: estado vacío", id);
            throw new RuntimeException("El estado es obligatorio");
        }

        Producto p = obtenerProducto(id);
        String estadoAnterior = p.getEstadoProducto();
        p.setEstadoProducto(nuevoEstado.toUpperCase());

        log.info("Estado de producto id={} cambió de {} a {}", id, estadoAnterior, p.getEstadoProducto());
        return aDto(p);
    }

    @Transactional(readOnly = true)
    public List<ProductoClienteResponseDTO> listarProductosCliente() {
        return productoRepository.findAll(Sort.by("nombreProducto"))
                .stream()
                .map(this::aClienteDto)
                .toList();
    }

    // Admin - Vendedor
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerProductoA(Long id) {
        return aDto(obtenerProducto(id));
    }

    // Cliente
    @Transactional(readOnly = true)
    public ProductoClienteResponseDTO obtenerProductoCliente(Long id) {
        return aClienteDto(obtenerProducto(id));
    }

    // ---------- privados ----------

    private void copiarDatos(Producto p, ProductoRequestDTO dto, String codigo) {
        p.setCodigoProducto(codigo);
        p.setNombreProducto(normalizar(dto.getNombreProducto()));
        p.setCategoriaProducto(normalizar(dto.getCategoriaProducto()));
        p.setPrecioProducto(dto.getPrecioProducto());
        p.setStockProducto(dto.getStockProducto());
        p.setEstadoProducto(normalizar(dto.getEstadoProducto()));
        p.setProveedor(obtenerProveedor(dto.getIdProveedor()));
    }

    private void validarCodigoDuplicado(String codigo, Long idActual) {
        if (codigo == null) return;

        boolean existe = (idActual == null)
                ? productoRepository.existsByCodigoProductoIgnoreCase(codigo)
                : productoRepository.existsByCodigoProductoIgnoreCaseAndIdProductoNot(codigo, idActual);

        if (existe) {
            log.warn("Código de producto duplicado rechazado: {}", codigo);
            throw new RuntimeException("Ya existe un producto con el código: " + codigo);
        }
    }

    private Proveedores obtenerProveedor(Long idProveedor) {
        if (idProveedor == null) return null;
        return proveedoresRepository.findById(idProveedor)
                .orElseThrow(() -> {
                    log.warn("Proveedor no encontrado al asignar a producto: id={}", idProveedor);
                    return new RuntimeException("Proveedor no encontrado: " + idProveedor);
                });
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado: id={}", id);
                    return new RuntimeException("Producto no encontrado: " + id);
                });
    }

    private ProductoResponseDTO aDto(Producto p) {
        return new ProductoResponseDTO(
                p.getCodigoProducto(),
                p.getNombreProducto(),
                p.getCategoriaProducto(),
                p.getPrecioProducto(),
                p.getStockProducto()
        );
    }

    private ProductoClienteResponseDTO aClienteDto(Producto p) {
        return new ProductoClienteResponseDTO(
                p.getNombreProducto(),
                p.getCategoriaProducto(),
                p.getPrecioProducto(),
                p.getStockProducto() > 0
        );
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}