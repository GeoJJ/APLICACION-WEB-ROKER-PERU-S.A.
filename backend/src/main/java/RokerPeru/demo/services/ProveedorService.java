package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProveedorRequestDTO;
import RokerPeru.demo.dto.ProveedorResponseDTO;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProveedoresRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedoresRepository proveedoresRepository;

    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO dto) {
        Proveedores p = new Proveedores();
        p.setNombreProveedor(dto.getNombreProveedor());
        p.setRucProveedor(dto.getRucProveedor());
        p.setTelefonoProveedor(dto.getTelefonoProveedor());
        p.setCorreoProveedor(dto.getCorreoProveedor());
        return aDto(proveedoresRepository.save(p));
    }

    public List<ProveedorResponseDTO> listarProveedor() {
        return proveedoresRepository.findAll().stream().map(this::aDto).toList();
    }

    public ProveedorResponseDTO buscarPorIdProveedor(Long id) {
        return aDto(proveedoresRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id)));
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
}