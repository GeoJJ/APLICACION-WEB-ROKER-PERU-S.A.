package RokerPeru.demo.controller;

import RokerPeru.demo.dto.AsignacionRequestDTO;
import RokerPeru.demo.dto.AsignacionResponseDTO;
import RokerPeru.demo.services.AsignacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping("/crear")
    public AsignacionResponseDTO crearAsignacion(@RequestBody AsignacionRequestDTO dto) {
        return asignacionService.crearAsignacion(dto);
    }

    @GetMapping("listar")
    public List<AsignacionResponseDTO> listarAsignacion() {
        return asignacionService.listarAsignacion();
    }

    @GetMapping("/buscar/{id}")
    public AsignacionResponseDTO buscarPorIdAsignacion(@PathVariable Long id) {
        return asignacionService.buscarPorIdAsignacion(id);
    }
}