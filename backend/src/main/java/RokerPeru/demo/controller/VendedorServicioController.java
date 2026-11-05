package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.services.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendedor/servicios")
@RequiredArgsConstructor
public class VendedorServicioController {

    private final ServicioService servicioService;

    // GET /vendedor/servicios
    @GetMapping
    public List<ServicioDTO.Response> listarServicios() {
        return servicioService.listarServicio();
    }

    // GET /vendedor/servicios/{id}
    @GetMapping("/{id}")
    public ServicioDTO.Response buscarServicio(@PathVariable Long id) {
        return servicioService.buscarPorIdServicio(id);
    }
}
