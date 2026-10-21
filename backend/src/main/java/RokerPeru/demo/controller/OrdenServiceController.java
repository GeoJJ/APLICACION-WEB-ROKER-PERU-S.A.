package RokerPeru.demo.controller;

import RokerPeru.demo.dto.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.OrdenServicioResponseDTO;
import RokerPeru.demo.services.OrdenServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordenes-servicio")
@RequiredArgsConstructor
public class OrdenServiceController {

    private final OrdenServicioService ordenService;

    @PostMapping("/crear")
    public OrdenServicioResponseDTO crearOrdenServicio(@RequestBody OrdenServicioRequestDTO dto) {
        return ordenService.crearOrdenServicio(dto);
    }

    @GetMapping("/listar")
    public List<OrdenServicioResponseDTO> listarOrdenServicio() {
        return ordenService.listarOrdenServicio();
    }

    @GetMapping("/buscar/{id}")
    public OrdenServicioResponseDTO buscarPorIdOrdenServicio(@PathVariable Long id) {
        return ordenService.buscarPorIdOrdenServicio(id);
    }
}