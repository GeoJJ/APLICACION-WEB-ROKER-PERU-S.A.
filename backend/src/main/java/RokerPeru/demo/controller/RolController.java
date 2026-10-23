package RokerPeru.demo.controller;

import RokerPeru.demo.dto.rol.RolRequestDTO;
import RokerPeru.demo.dto.rol.RolResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.services.RolService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RolController {
    private final RolService rolService;

    @PostMapping
    public RolResponseDTO crearRol(@Valid @RequestBody RolRequestDTO dto) {
        return rolService.crearRol(dto);
    }

    @GetMapping("/listar")
    public List<RolResponseDTO> listarRol() {
        return rolService.listarRol();
    }

    // GET /roles/buscar?nombre=adm
    @GetMapping("/buscar")
        public List<RolResponseDTO> buscarRol(@RequestParam String nombre) {
            return rolService.buscarRol(nombre);
        }

    @PutMapping("/{id}")
    public RolResponseDTO actualizarRol(@PathVariable Long id, @Valid @RequestBody RolRequestDTO dto) {
        return rolService.actualizarRol(id, dto);
    }
}
