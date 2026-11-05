package RokerPeru.demo.services;

import RokerPeru.demo.dto.RolDTO;
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
    public RolDTO.Response crearRol(RolDTO.Request dto) {
        if (rolRepository.existsByNombreRolIgnoreCase(dto.nombreRol())) {
            log.warn("Creación de rol rechazada, nombre duplicado: {}", dto.nombreRol());
            throw new RuntimeException("Ya existe un rol con el nombre: " + dto.nombreRol());
        }
        Rol guardado = rolRepository.save(dto.toEntity());
        log.info("Rol creado: id={}, nombre={}", guardado.getIdRol(), dto.nombreRol());
        return RolDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<RolDTO.Response> listarRol() {
        List<RolDTO.Response> roles = rolRepository.findAll().stream()
                .map(RolDTO.Response::fromEntity)
                .toList();

        log.debug("Listado de roles: {} resultados", roles.size());
        return roles;
    }

    @Transactional
    public RolDTO.Response actualizarRol(Long id, RolDTO.Request dto) {
        Rol rol = obtenerRol(id);

        if (rolRepository.existsByNombreRolIgnoreCaseAndIdRolNot(dto.nombreRol(), id)) {
            log.warn("Actualización de rol id={} rechazada, nombre duplicado: {}", id, dto.nombreRol());
            throw new RuntimeException("Ya existe otro rol con el nombre: " + dto.nombreRol());
        }
        String nombreAnterior = rol.getNombreRol();
        dto.applyTo(rol);

        log.info("Rol actualizado: id={}, '{}' -> '{}'", id, nombreAnterior, dto.nombreRol());
        return RolDTO.Response.fromEntity(rol);
    }

    @Transactional(readOnly = true)
    public List<RolDTO.Response> buscarRol(String texto) {
        List<RolDTO.Response> roles = rolRepository.buscarRolPorNombre(texto.trim())
                .stream().map(RolDTO.Response::fromEntity).toList();

        log.debug("Búsqueda de roles con texto='{}': {} resultados", texto.trim(), roles.size());
        return roles;
    }

    public Rol obtenerRol(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rol no encontrado: id={}", id);
                    return new RuntimeException("Rol no encontrado: " + id);
                });
    }
}