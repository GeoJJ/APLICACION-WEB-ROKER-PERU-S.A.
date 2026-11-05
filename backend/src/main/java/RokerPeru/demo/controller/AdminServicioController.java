package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ServicioDTO;
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
    public ServicioDTO.Response crearServicio(@Valid @RequestBody ServicioDTO.Request dto) {
        return servicioService.crearServicio(dto);
    }

    // GET /admin/servicios
    @GetMapping
    public List<ServicioDTO.Response> listarServicio() {
        return servicioService.listarServicio();
    }

    // GET /admin/servicios/{id}
    @GetMapping("/{id}")
    public ServicioDTO.Response buscarServicio(@PathVariable Long id) {
        return servicioService.buscarPorIdServicio(id);
    }

    // PUT /admin/servicios/{id}
    @PutMapping("/{id}")
    public ServicioDTO.Response actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioDTO.Request dto) {
        return servicioService.actualizarServicio(id, dto);
    }

    // DELETE /admin/servicios/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
    }
}
