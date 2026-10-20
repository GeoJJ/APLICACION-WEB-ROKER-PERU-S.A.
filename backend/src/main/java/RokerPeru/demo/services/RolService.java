package RokerPeru.demo.services;

import RokerPeru.demo.dto.RolRequestDTO;
import RokerPeru.demo.dto.RolResponseDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public RolResponseDTO crearRol(RolRequestDTO dto) {
        Rol rol = new Rol();
        rol.setNombreRol(dto.getNombreRol());
        return aDto(rolRepository.save(rol));
    }

    public List<RolResponseDTO> listarRol() {
        return rolRepository.findAll().stream()
                .map(this::aDto)
                .toList();
    }

    public RolResponseDTO buscarPorId(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + id));
        return aDto(rol);
    }

    private RolResponseDTO aDto(Rol rol) {
        return new RolResponseDTO(rol.getIdRol(), rol.getNombreRol());
    }
}