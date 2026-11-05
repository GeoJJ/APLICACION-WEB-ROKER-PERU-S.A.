package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.services.ServicioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cliente/servicios")
@RequiredArgsConstructor
public class ClienteServicioController {

    private final ServicioService servicioService;

    // GET /cliente/servicios
    @GetMapping
    public List<ServicioDTO.ClienteResponse> listarServiciosCliente() {
        return servicioService.listarServiciosCliente();
    }

    // GET /cliente/servicios/buscar?texto=...
    @GetMapping("/buscar")
    public List<ServicioDTO.ClienteResponse> buscarServiciosCliente(@RequestParam String texto) {
        return servicioService.buscarServiciosCliente(texto);
    }
}
