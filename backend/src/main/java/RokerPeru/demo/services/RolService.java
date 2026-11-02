package RokerPeru.demo.services;

import RokerPeru.demo.dto.rol.RolRequestDTO;
import RokerPeru.demo.dto.rol.RolResponseDTO;
import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    @Transactional
    public RolResponseDTO crearRol(RolRequestDTO dto) {
        String nombre = normalizar(dto.getNombreRol());

        if (rolRepository.existsByNombreRolIgnoreCase(nombre)) {
            log.warn("Creación de rol rechazada, nombre duplicado: {}", nombre);
            throw new RuntimeException("Ya existe un rol con el nombre: " + nombre);
        }

        Rol rol = new Rol();
        rol.setNombreRol(nombre);
        Rol guardado = rolRepository.save(rol);

        log.info("Rol creado: id={}, nombre={}", guardado.getIdRol(), nombre);
        return aDto(guardado);
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> listarRol() {
        List<RolResponseDTO> roles = rolRepository.findAll().stream()
                .map(this::aDto)
                .toList();

        log.debug("Listado de roles: {} resultados", roles.size());
        return roles;
    }

    @Transactional
    public RolResponseDTO actualizarRol(Long id, RolRequestDTO dto) {
        Rol rol = obtenerRol(id);
        String nombre = normalizar(dto.getNombreRol());

        if (rolRepository.existsByNombreRolIgnoreCaseAndIdRolNot(nombre, id)) {
            log.warn("Actualización de rol id={} rechazada, nombre duplicado: {}", id, nombre);
            throw new RuntimeException("Ya existe otro rol con el nombre: " + nombre);
        }

        String nombreAnterior = rol.getNombreRol();
        rol.setNombreRol(nombre);

        log.info("Rol actualizado: id={}, '{}' -> '{}'", id, nombreAnterior, nombre);
        return aDto(rol);
    }

    @Transactional(readOnly = true)
    public List<RolResponseDTO> buscarRol(String texto) {
        List<RolResponseDTO> roles = rolRepository.buscarRolPorNombre(texto.trim())
                .stream().map(this::aDto).toList();

        log.debug("Búsqueda de roles con texto='{}': {} resultados", texto.trim(), roles.size());
        return roles;
    }

    private RolResponseDTO aDto(Rol rol) {
        return new RolResponseDTO(rol.getIdRol(), rol.getNombreRol());
    }

    private String normalizar(String nombre) {
        return nombre.trim();
    }

    private Rol obtenerRol(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rol no encontrado: id={}", id);
                    return new RuntimeException("Rol no encontrado: " + id);
                });
    }
}