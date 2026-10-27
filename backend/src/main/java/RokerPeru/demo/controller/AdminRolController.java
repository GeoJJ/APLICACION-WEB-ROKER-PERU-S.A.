package RokerPeru.demo.controller;

import RokerPeru.demo.dto.rol.RolRequestDTO;
import RokerPeru.demo.dto.rol.RolResponseDTO;
import RokerPeru.demo.services.RolService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRolController {

    private final RolService rolService;

    // POST /admin/roles
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponseDTO crearRol(@Valid @RequestBody RolRequestDTO dto) {
        return rolService.crearRol(dto);
    }

    // GET /admin/roles
    @GetMapping
    public List<RolResponseDTO> listarRol() {
        return rolService.listarRol();
    }

    // GET /admin/roles/buscar?texto=...
    @GetMapping("/buscar")
    public List<RolResponseDTO> buscarRol(@RequestParam String texto) {
        return rolService.buscarRol(texto);
    }

    // PUT /admin/roles/{id}
    @PutMapping("/{id}")
    public RolResponseDTO actualizarRol(@PathVariable Long id, @Valid @RequestBody RolRequestDTO dto) {
        return rolService.actualizarRol(id, dto);
    }
}