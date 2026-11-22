package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProductoDTO;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.enums.TipoCodigoEnum;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedoresRepository proveedoresRepository;
    private final CodigoService codigoService;

    @Transactional
    public ProductoDTO.Response crearProducto(ProductoDTO.Request dto) {
        Producto producto = dto.toEntity(obtenerProveedor(dto.idProveedor()));
        // El código (PRD-0001) lo asigna el backend; no viene del formulario
        producto.setCodigoProducto(codigoService.generar(TipoCodigoEnum.PRODUCTO));

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado: id={}, codigo={}", guardado.getIdProducto(), guardado.getCodigoProducto());

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

        // El código nunca cambia al editar: applyTo no lo toca
        dto.applyTo(producto, obtenerProveedor(dto.idProveedor()));
        log.info("Producto actualizado: id={}, codigo={}", id, producto.getCodigoProducto());

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
            throw new ReglaNegocioException("El estado del producto es obligatorio y no puede estar vacío");
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor", idProveedor));
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));     }
}