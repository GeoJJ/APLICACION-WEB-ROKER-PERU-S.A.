package RokerPeru.demo.services;

import RokerPeru.demo.dto.EmpleadoServicioDTO;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmpleadoServicioService {

    private final EmpleadosServicioRepository empleadosRepository;

    @Transactional
    public EmpleadoServicioDTO.Response crearEmpleadoServicio(EmpleadoServicioDTO.Request dto) {
        EmpleadosServicio empleado = dto.toEntity();
        EmpleadosServicio guardado = empleadosRepository.save(empleado);

        log.info("Empleado de servicio creado: {} {}", guardado.getNombreEmpleadoServicio(), guardado.getApellidoEmpleadoServicio());
        return EmpleadoServicioDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<EmpleadoServicioDTO.Response> listarEmpleadoServicio() {
        return empleadosRepository.findAll(Sort.by("nombreEmpleadoServicio"))
                .stream()
                .map(EmpleadoServicioDTO.Response::fromEntity) // Referencia directa al método estático
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoServicioDTO.Response> buscarEmpleadoServicio(String texto) {
        return empleadosRepository.buscarEmpleadoServicio(texto.trim())
                .stream()
                .map(EmpleadoServicioDTO.Response::fromEntity)
                .toList();
    }

    @Transactional
    public EmpleadoServicioDTO.Response actualizarEmpleadoServicio(Long id, EmpleadoServicioDTO.Request dto) {
        EmpleadosServicio empleado = obtenerEmpleadoServicio(id);

        dto.aplicarDatosA(empleado);

        log.info("Empleado de servicio actualizado: id={}", id);
        return EmpleadoServicioDTO.Response.fromEntity(empleado);
    }

    @Transactional
    public void eliminarEmpleadoServicio(Long id) {
        EmpleadosServicio empleado = obtenerEmpleadoServicio(id);
        empleadosRepository.delete(empleado);
        log.info("Empleado de servicio eliminado: id={}", id);
    }

    private EmpleadosServicio obtenerEmpleadoServicio(Long id) {
        return empleadosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado de servicio no encontrado: " + id));
    }
}