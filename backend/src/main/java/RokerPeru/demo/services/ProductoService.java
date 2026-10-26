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
        return aDto(productoRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarProducto() {
        return productoRepository.findAll(Sort.by("nombreProducto"))
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorIdProducto(Long id) {
        return aDto(obtenerProducto(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarProducto(String texto) {
        return productoRepository.buscarProducto(texto.trim())
                .stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional
    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO dto) {
        Producto p = obtenerProducto(id);
        String codigo = normalizar(dto.getCodigoProducto());
        validarCodigoDuplicado(codigo, id);

        copiarDatos(p, dto, codigo);
        return aDto(p);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productoRepository.delete(obtenerProducto(id));
    }

    @Transactional
    public ProductoResponseDTO cambiarEstadoProducto(Long id, String estado) {
        String nuevoEstado = normalizar(estado);
        if (nuevoEstado == null) {
            throw new RuntimeException("El estado es obligatorio");
        }
        Producto p = obtenerProducto(id);
        p.setEstadoProducto(nuevoEstado.toUpperCase());
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
            throw new RuntimeException("Ya existe un producto con el código: " + codigo);
        }
    }


    private Proveedores obtenerProveedor(Long idProveedor) {
        if (idProveedor == null) return null;
        return proveedoresRepository.findById(idProveedor)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + idProveedor));
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

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }
}