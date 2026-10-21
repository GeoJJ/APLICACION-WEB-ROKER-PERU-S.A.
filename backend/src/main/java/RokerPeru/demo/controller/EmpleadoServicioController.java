package RokerPeru.demo.controller;
import RokerPeru.demo.dto.EmpleadoRequestDTO;
import RokerPeru.demo.dto.EmpleadoResponseDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoServicioController {

    private final EmpleadoServicioService empleadoService;

    @PostMapping("/crear")
    public EmpleadoResponseDTO crearEmpleadoServicio(@RequestBody EmpleadoRequestDTO dto) {
        return empleadoService.crearEmpleadoServicio(dto);
    }

    @GetMapping("/listar")
    public List<EmpleadoResponseDTO> listarEmpleadoServicio() {
        return empleadoService.listarEmpleadoServicio();
    }

    @GetMapping("/buscar/{id}")
    public EmpleadoResponseDTO buscarPorIdEmpleadoServicio(@PathVariable Long id) {
        return empleadoService.buscarPorIdEmpleadoServicio(id);
    }
}