package RokerPeru.demo.services;

import RokerPeru.demo.dto.AsignacionDTO;
import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.exception.ConflictoException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.AsignacionesRepository;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionesRepository asignacionesRepository;
    private final OrdenesServicioRepository ordenesRepository;
    private final EmpleadosServicioRepository empleadosRepository;

    // VENDEDOR: Asignar empleado a orden de servicio
    @Transactional
    public AsignacionDTO.Response crearAsignacion(AsignacionDTO.RequestAsignacion dto) {
        OrdenesServicio orden = ordenesRepository.findById(dto.idOrdenServicio())
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio", dto.idOrdenServicio()));

        EmpleadosServicio empleado = empleadosRepository.findById(dto.idEmpleado())
                .orElseThrow(() -> new RuntimeException("Empleado de servicio no encontrado: " + dto.idEmpleado()));

        // Validar que no esté duplicada la asignación
        if (asignacionesRepository.existsByOrdenServicio_IdOrdenServicioAndEmpleado_IdEmpleadoServicio(
                dto.idOrdenServicio(), dto.idEmpleado())) {
            throw new ConflictoException("Este empleado ya está asignado a esta orden");
        }

        Asignaciones asignacion = dto.toEntity(orden, empleado);
        Asignaciones guardada = asignacionesRepository.save(asignacion);

        log.info("Vendedor asignó al empleado {} a la orden #{}", empleado.getNombreEmpleadoServicio(), orden.getIdOrdenServicio());

        return AsignacionDTO.Response.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public List<AsignacionDTO.Response> listarAsignacionesPorOrden(Long idOrden) {
        return asignacionesRepository.findByOrdenServicio_IdOrdenServicioOrderByFechaAsignacionDesc(idOrden)
                .stream()
                .map(AsignacionDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionDTO.Response> listarAsignacionesPorEmpleado(Long idEmpleado) {
        return asignacionesRepository.findByEmpleado_IdEmpleadoServicioOrderByFechaAsignacionDesc(idEmpleado)
                .stream()
                .map(AsignacionDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionDTO.Response> listarTodasAsignaciones() {
        return asignacionesRepository.findAllByOrderByFechaAsignacionDesc()
                .stream()
                .map(AsignacionDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AsignacionDTO.Response buscarAsignacionPorId(Long id) {
        return asignacionesRepository.findById(id)
                .map(AsignacionDTO.Response::fromEntity)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada: " + id));
    }
}