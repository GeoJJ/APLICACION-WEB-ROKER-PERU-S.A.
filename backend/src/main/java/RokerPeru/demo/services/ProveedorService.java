package RokerPeru.demo.services;

import RokerPeru.demo.dto.ProveedorDTO;
import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
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
public class ProveedorService {

    private final ProveedoresRepository proveedoresRepository;

    @Transactional
    public ProveedorDTO.Response crearProveedor(ProveedorDTO.Request dto) {
        if (proveedoresRepository.existsByRucProveedor(dto.rucProveedor())) {
            throw new ConflictoException("Ya existe un proveedor con el RUC: " + dto.rucProveedor());
        }

        Proveedores guardado = proveedoresRepository.save(dto.toEntity());
        log.info("Proveedor creado: id={}, ruc={}", guardado.getIdProveedor(), dto.rucProveedor());

        return ProveedorDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO.Response> listarProveedor() {
        return proveedoresRepository.findAll(Sort.by("nombreProveedor"))
                .stream()
                .map(ProveedorDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO.Response> buscarProveedor(String texto) {
        return proveedoresRepository.buscarProveedor(texto.trim())
                .stream()
                .map(ProveedorDTO.Response::fromEntity)
                .toList();
    }

    @Transactional
    public ProveedorDTO.Response actualizarProveedor(Long id, ProveedorDTO.Request dto) {
        Proveedores proveedor = obtenerProveedor(id);

        if (proveedoresRepository.existsByRucProveedorAndIdProveedorNot(dto.rucProveedor(), id)) {
            throw new ConflictoException("Ya existe otro proveedor con el RUC: " + dto.rucProveedor());
        }

        dto.applyTo(proveedor);
        log.info("Proveedor actualizado: id={}, ruc={}", id, dto.rucProveedor());

        return ProveedorDTO.Response.fromEntity(proveedor);
    }

    @Transactional
    public void eliminarProveedor(Long id) {
        Proveedores proveedor = obtenerProveedor(id);
        proveedoresRepository.delete(proveedor);
        log.info("Proveedor eliminado: id={}", id);
    }

    // Métodos privados
    private Proveedores obtenerProveedor(Long id) {
        return proveedoresRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor", id));     }
}
