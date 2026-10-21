package RokerPeru.demo.services;

import RokerPeru.demo.dto.EmpleadoRequestDTO;
import RokerPeru.demo.dto.EmpleadoResponseDTO;
import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoServicioService {

    private final EmpleadosServicioRepository empleadosRepository;

    public EmpleadoResponseDTO crearEmpleadoServicio(EmpleadoRequestDTO dto) {
        EmpleadosServicio e = new EmpleadosServicio();
        e.setNombreEmpleadoServicio(dto.getNombreEmpleadoServicio());
        e.setApellidoEmpleadoServicio(dto.getApellidoEmpleadoServicio());
        e.setTelefonoEmpleadoServicio(dto.getTelefonoEmpleadoServicio());
        e.setCargoEmpleadoServicio(dto.getCargoEmpleadoServicio());
        return aDto(empleadosRepository.save(e));
    }

    public List<EmpleadoResponseDTO> listarEmpleadoServicio() {
        return empleadosRepository.findAll().stream().map(this::aDto).toList();
    }

    public EmpleadoResponseDTO buscarPorIdEmpleadoServicio(Long id) {
        return aDto(empleadosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + id)));
    }

    private EmpleadoResponseDTO aDto(EmpleadosServicio e) {
        return new EmpleadoResponseDTO(
                e.getIdEmpleadoServicio(),
                e.getNombreEmpleadoServicio(),
                e.getApellidoEmpleadoServicio(),
                e.getTelefonoEmpleadoServicio(),
                e.getCargoEmpleadoServicio()
        );
    }
}