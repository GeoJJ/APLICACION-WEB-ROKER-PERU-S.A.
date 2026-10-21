package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioRequestDTO;
import RokerPeru.demo.dto.ServicioResponseDTO;
import RokerPeru.demo.services.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping("/crear")
    public ServicioResponseDTO crearServicio(@RequestBody ServicioRequestDTO dto) {
        return servicioService.crearServicio(dto);
    }

    @GetMapping("/listar")
    public List<ServicioResponseDTO> listarServicio() {
        return servicioService.listarServicio();
    }

    @GetMapping("/buscar/{id}")
    public ServicioResponseDTO buscarPorIdServicio(@PathVariable Long id) {
        return servicioService.buscarPorIdServicio(id);
    }
}