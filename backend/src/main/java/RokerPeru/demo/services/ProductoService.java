package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProductoRequestDTO;
import RokerPeru.demo.dto.ProductoResponseDTO;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProveedoresRepository proveedoresRepository;

    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {
        Producto p = new Producto();
        p.setCodigoProducto(dto.getCodigoProducto());
        p.setNombreProducto(dto.getNombreProducto());
        p.setCategoriaProducto(dto.getCategoriaProducto());
        p.setPrecioProducto(dto.getPrecioProducto());
        p.setStockProducto(dto.getStockProducto());
        p.setEstadoProducto(dto.getEstadoProducto());

        if (dto.getIdProveedor() != null) {
            Proveedores proveedor = proveedoresRepository.findById(dto.getIdProveedor())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + dto.getIdProveedor()));
            p.setProveedor(proveedor);
        }

        return aDto(productoRepository.save(p));
    }

    public List<ProductoResponseDTO> listarProducto() {
        return productoRepository.findAll().stream().map(this::aDto).toList();
    }

    public ProductoResponseDTO buscarPorIdProducto(Long id) {
        return aDto(productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id)));
    }

    private ProductoResponseDTO aDto(Producto p) {
        return new ProductoResponseDTO(
                p.getIdProducto(),
                p.getCodigoProducto(),
                p.getNombreProducto(),
                p.getCategoriaProducto(),
                p.getPrecioProducto(),
                p.getStockProducto(),
                p.getEstadoProducto(),
                p.getProveedor() != null ? p.getProveedor().getNombreProveedor() : null
        );
    }
}