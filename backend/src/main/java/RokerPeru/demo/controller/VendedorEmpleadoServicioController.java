package RokerPeru.demo.controller;

import RokerPeru.demo.dto.empleadoServicio.EmpleadoResponseDTO;
import RokerPeru.demo.services.EmpleadoServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/empleados-servicio")
@RequiredArgsConstructor
public class VendedorEmpleadoServicioController {

    private final EmpleadoServicioService empleadoServicioService;

    // GET /vendedor/empleados-servicio
    @GetMapping
    public List<EmpleadoResponseDTO> listarEmpleadosServicio() {
        return empleadoServicioService.listarEmpleadoServicio();
    }

    // GET /vendedor/empleados-servicio/buscar?texto=...
    @GetMapping("/buscar")
    public List<EmpleadoResponseDTO> buscarEmpleadoServicio(@RequestParam String texto) {
        return empleadoServicioService.buscarPorIdEmpleadoServicio(texto);
    }
}
