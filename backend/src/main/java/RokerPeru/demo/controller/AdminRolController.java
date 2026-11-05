package RokerPeru.demo.controller;

import RokerPeru.demo.dto.RolDTO;
import RokerPeru.demo.services.RolService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class AdminRolController {

    private final RolService rolService;

    // POST /admin/roles
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolDTO.Response crearRol(@Valid @RequestBody RolDTO.Request dto) {
        return rolService.crearRol(dto);
    }

    // GET /admin/roles
    @GetMapping
    public List<RolDTO.Response> listarRol() {
        return rolService.listarRol();
    }

    // GET /admin/roles/buscar?texto=...
    @GetMapping("/buscar")
    public List<RolDTO.Response> buscarRol(@RequestParam String texto) {
        return rolService.buscarRol(texto);
    }

    // PUT /admin/roles/{id}
    @PutMapping("/{id}")
    public RolDTO.Response actualizarRol(@PathVariable Long id, @Valid @RequestBody RolDTO.Request dto) {
        return rolService.actualizarRol(id, dto);
    }
}