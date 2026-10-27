package RokerPeru.demo.services;

import RokerPeru.demo.dto.rol.RolRequestDTO;
import RokerPeru.demo.dto.rol.RolResponseDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    @Transactional
    public RolResponseDTO crearRol(RolRequestDTO dto) {
        String nombre = normalizar(dto.getNombreRol());

        if (rolRepository.existsByNombreRolIgnoreCase(nombre)) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + nombre);
        }

        Rol rol = new Rol();
        rol.setNombreRol(nombre);
        return aDto(rolRepository.save(rol));
    }
    @Transactional(readOnly = true)
    public List<RolResponseDTO> listarRol() {
        return rolRepository.findAll().stream()
                .map(this::aDto)
                .toList();
    }

    @Transactional
    public RolResponseDTO actualizarRol(Long id, RolRequestDTO dto) {
        Rol rol = obtenerRol(id);
        String nombre = normalizar(dto.getNombreRol());

        if (rolRepository.existsByNombreRolIgnoreCaseAndIdRolNot(nombre, id)) {
            throw new RuntimeException("Ya existe otro rol con el nombre: " + nombre);
        }

        rol.setNombreRol(nombre);
        return aDto(rol);
    }
    @Transactional(readOnly = true)
    public List<RolResponseDTO> buscarRol(String texto) {
        return rolRepository.buscarRolPorNombre(texto.trim())
                .stream().map(this::aDto).toList();
    }

    private RolResponseDTO aDto(Rol rol) {
        return new RolResponseDTO(rol.getIdRol(), rol.getNombreRol());
    }

    private String normalizar(String nombre) {
        return nombre.trim();
    }

    private Rol obtenerRol(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));
    }
}