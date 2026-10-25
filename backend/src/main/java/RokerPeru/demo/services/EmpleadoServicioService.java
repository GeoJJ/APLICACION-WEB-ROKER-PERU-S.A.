package RokerPeru.demo.services;

import RokerPeru.demo.dto.empleadoServicio.EmpleadoRequestDTO;
import RokerPeru.demo.dto.empleadoServicio.EmpleadoResponseDTO;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoServicioService {

    private final EmpleadosServicioRepository empleadosRepository;
    @Transactional
    public EmpleadoResponseDTO crearEmpleadoServicio(EmpleadoRequestDTO dto) {
        EmpleadosServicio e = new EmpleadosServicio();
        e.setNombreEmpleadoServicio(normalizar(dto.getNombreEmpleadoServicio()));
        e.setApellidoEmpleadoServicio(normalizar(dto.getApellidoEmpleadoServicio()));
        e.setTelefonoEmpleadoServicio(dto.getTelefonoEmpleadoServicio());
        return aDto(empleadosRepository.save(e));
    }
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> listarEmpleadoServicio() {
        return empleadosRepository.findAll(Sort.by("nombreEmpleadoServicio")).stream().map(this::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> buscarPorIdEmpleadoServicio(String texto) {
        return empleadosRepository.buscarEmpleadoServicio(texto).stream().map(this::aDto).toList();
    }

    @Transactional
    public EmpleadoResponseDTO actualizarEmpleadoServicio(Long id,EmpleadoRequestDTO dto) {
        EmpleadosServicio e= obtenerEmpleadoServicio(id);
        e.setNombreEmpleadoServicio(normalizar(dto.getNombreEmpleadoServicio()));
        e.setApellidoEmpleadoServicio(normalizar(dto.getApellidoEmpleadoServicio()));
        e.setTelefonoEmpleadoServicio(dto.getTelefonoEmpleadoServicio());
        return aDto(e);
    }
    @Transactional
    public void eliminarEmpleadoServicio(Long id){
        EmpleadosServicio empleadosServicio= obtenerEmpleadoServicio(id);
        empleadosRepository.delete(empleadosServicio);
    }

    private EmpleadoResponseDTO aDto(EmpleadosServicio e) {
        return new EmpleadoResponseDTO(
                e.getIdEmpleadoServicio(),
                e.getNombreEmpleadoServicio(),
                e.getApellidoEmpleadoServicio(),
                e.getTelefonoEmpleadoServicio()
        );
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto .trim();
    }

    private EmpleadosServicio obtenerEmpleadoServicio(Long id){
        return empleadosRepository.findById(id).orElseThrow(() -> new RuntimeException("Empleado de servicio no encontrado: " + id));
    }
}