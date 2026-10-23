package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProveedorResponseDTO;
import RokerPeru.demo.dto.proveedor.ProveedorRequestDTO;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {
    private final ProveedoresRepository proveedoresRepository;

    @Transactional
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO dto) {
        Proveedores p = new Proveedores();
        String rucProveedor= normalizar(dto.getRucProveedor());
        if(proveedoresRepository.existsByRucProveedor(rucProveedor)){
            throw new RuntimeException("Ya existe un proveedor con el RUC: " + rucProveedor);
        }
        p.setNombreProveedor(dto.getNombreProveedor());
        p.setRucProveedor(rucProveedor);
        p.setTelefonoProveedor(dto.getTelefonoProveedor());
        p.setCorreoProveedor(dto.getCorreoProveedor());
        return aDto(proveedoresRepository.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> listarProveedor() {
        return proveedoresRepository.findAll(Sort.by("nombreProveedor"))
                .stream()
                .map(this::aDto)
                .toList();    }

    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> buscarProveedor(String texto) {
        return proveedoresRepository.buscarProveedor(texto).stream().map(this::aDto).toList();
    }

    @Transactional
    public ProveedorResponseDTO actualizarProveedor(Long id, ProveedorRequestDTO dto) {
        Proveedores proveedor = obtenerProveedor(id);
        String ruc = normalizar(dto.getRucProveedor());

        if (proveedoresRepository.existsByRucProveedorAndIdProveedorNot(ruc, id)) {
            throw new RuntimeException("Ya existe otro proveedor con el RUC: " + ruc);
        }

        proveedor.setNombreProveedor(dto.getNombreProveedor());
        proveedor.setRucProveedor(ruc);
        proveedor.setTelefonoProveedor(normalizar(dto.getTelefonoProveedor()));
        proveedor.setCorreoProveedor(normalizar(dto.getCorreoProveedor()));
        return aDto(proveedor);
    }

    @Transactional
    public void eliminarProveedor(Long id) {
        Proveedores proveedor = obtenerProveedor(id);
        proveedoresRepository.delete(proveedor);
    }

    private Proveedores obtenerProveedor(Long id){
        return proveedoresRepository.findById(id).orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id));
    }

    private ProveedorResponseDTO aDto(Proveedores p) {
        return new ProveedorResponseDTO(
                p.getIdProveedor(),
                p.getNombreProveedor(),
                p.getRucProveedor(),
                p.getTelefonoProveedor(),
                p.getCorreoProveedor()
        );
    }
    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto .trim();
    }
}