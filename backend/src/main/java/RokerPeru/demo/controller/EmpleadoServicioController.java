package RokerPeru.demo.controller;

import RokerPeru.demo.dto.empleadoServicio.EmpleadoRequestDTO;
import RokerPeru.demo.dto.empleadoServicio.EmpleadoResponseDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoServicioController {

    private final EmpleadoServicioService empleadoService;

    @PostMapping
    public EmpleadoResponseDTO crearEmpleadoServicio(@RequestBody EmpleadoRequestDTO dto) {
        return empleadoService.crearEmpleadoServicio(dto);
    }

    @GetMapping("/listar")
    public List<EmpleadoResponseDTO> listarEmpleadoServicio() {
        return empleadoService.listarEmpleadoServicio();
    }

    @GetMapping("/buscar")
    public List<EmpleadoResponseDTO> buscarPorIdEmpleadoServicio(@RequestParam String texto) {
        return empleadoService.buscarPorIdEmpleadoServicio(texto);
    }

    @PutMapping("{id}")
    public EmpleadoResponseDTO actualizarEmpleadoServicio(@PathVariable Long id, @RequestBody EmpleadoRequestDTO dto) {
        return empleadoService.actualizarEmpleadoServicio(id, dto);
    }

    @DeleteMapping("{id}")
    public void eliminarEmpleadoServicio(@PathVariable Long id) {
        empleadoService.eliminarEmpleadoServicio(id);
    }
}