package RokerPeru.demo.controller;

import RokerPeru.demo.dto.empleadoServicio.EmpleadoRequestDTO;
import RokerPeru.demo.dto.empleadoServicio.EmpleadoResponseDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/empleados-servicio")
@RequiredArgsConstructor
public class AdminEmpleadoServicioController {

    private final EmpleadoServicioService empleadoServicioService;

    // POST /admin/empleados-servicio
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponseDTO crearEmpleadoServicio(@Valid @RequestBody EmpleadoRequestDTO dto) {
        return empleadoServicioService.crearEmpleadoServicio(dto);
    }

    // GET /admin/empleados-servicio
    @GetMapping
    public List<EmpleadoResponseDTO> listarEmpleadoServicio() {
        return empleadoServicioService.listarEmpleadoServicio();
    }

    // GET /admin/empleados-servicio/buscar?texto=...
    @GetMapping("/buscar")
    public List<EmpleadoResponseDTO> buscarEmpleadoServicio(@RequestParam String texto) {
        return empleadoServicioService.buscarPorIdEmpleadoServicio(texto);
    }

    // PUT /admin/empleados-servicio/{id}
    @PutMapping("/{id}")
    public EmpleadoResponseDTO actualizarEmpleadoServicio(@PathVariable Long id, @Valid @RequestBody EmpleadoRequestDTO dto) {
        return empleadoServicioService.actualizarEmpleadoServicio(id, dto);
    }

    // DELETE /admin/empleados-servicio/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarEmpleadoServicio(@PathVariable Long id) {
        empleadoServicioService.eliminarEmpleadoServicio(id);
    }
}
