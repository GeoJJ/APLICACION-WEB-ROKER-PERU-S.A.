package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioRequestDTO;
import RokerPeru.demo.dto.ServicioResponseDTO;
import RokerPeru.demo.services.ServicioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/servicios")
@RequiredArgsConstructor
public class AdminServicioController {

    private final ServicioService servicioService;

    // POST /admin/servicios
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponseDTO crearServicio(@Valid @RequestBody ServicioRequestDTO dto) {
        return servicioService.crearServicio(dto);
    }

    // GET /admin/servicios
    @GetMapping
    public List<ServicioResponseDTO> listarServicio() {
        return servicioService.listarServicio();
    }

    // GET /admin/servicios/{id}
    @GetMapping("/{id}")
    public ServicioResponseDTO buscarServicio(@PathVariable Long id) {
        return servicioService.buscarPorIdServicio(id);
    }

    // PUT /admin/servicios/{id}
    @PutMapping("/{id}")
    public ServicioResponseDTO actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioRequestDTO dto) {
        return servicioService.actualizarServicio(id, dto);
    }

    // DELETE /admin/servicios/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
    }
}
