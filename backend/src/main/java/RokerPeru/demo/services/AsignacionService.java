package RokerPeru.demo.services;

import RokerPeru.demo.dto.AsignacionRequestDTO;
import RokerPeru.demo.dto.AsignacionResponseDTO;
import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.repository.AsignacionesRepository;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionesRepository asignacionesRepository;
    private final OrdenesServicioRepository ordenesRepository;
    private final EmpleadosServicioRepository empleadosRepository;

    public AsignacionResponseDTO crearAsignacion(AsignacionRequestDTO dto) {
        OrdenesServicio orden = ordenesRepository.findById(dto.getIdOrdenServicio())
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + dto.getIdOrdenServicio()));

        EmpleadosServicio empleado = empleadosRepository.findById(dto.getIdEmpleado())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + dto.getIdEmpleado()));

        Asignaciones a = new Asignaciones();
        a.setFechaAsignacion(LocalDateTime.now());   // lo pone el servidor
        a.setNombreResponsableAsignacion(dto.getNombreResponsableAsignacion());
        a.setDescripcionAsignacion(dto.getDescripcionAsignacion());
        a.setOrdenServicio(orden);
        a.setEmpleado(empleado);

        return aDto(asignacionesRepository.save(a));
    }

    public List<AsignacionResponseDTO> listarAsignacion() {
        return asignacionesRepository.findAll().stream().map(this::aDto).toList();
    }

    public AsignacionResponseDTO buscarPorIdAsignacion(Long id) {
        return aDto(asignacionesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada: " + id)));
    }

    private AsignacionResponseDTO aDto(Asignaciones a) {
        return new AsignacionResponseDTO(
                a.getIdAsignacion(),
                a.getNombreResponsableAsignacion(),
                a.getFechaAsignacion(),
                a.getDescripcionAsignacion(),
                a.getOrdenServicio().getIdOrdenServicio(),
                a.getEmpleado().getIdEmpleadoServicio(),
                a.getEmpleado().getNombreEmpleadoServicio() + " " + a.getEmpleado().getApellidoEmpleadoServicio()
        );
    }
}