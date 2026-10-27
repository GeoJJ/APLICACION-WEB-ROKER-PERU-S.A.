package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioClienteResponseDTO;
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
    public List<ServicioClienteResponseDTO> listarServiciosCliente() {
        return servicioService.listarServiciosCliente();
    }

    // GET /cliente/servicios/buscar?texto=...
    @GetMapping("/buscar")
    public List<ServicioClienteResponseDTO> buscarServiciosCliente(@RequestParam String texto) {
        return servicioService.buscarServiciosCliente(texto);
    }
}
