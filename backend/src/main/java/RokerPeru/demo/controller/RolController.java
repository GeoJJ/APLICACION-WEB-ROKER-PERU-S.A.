package RokerPeru.demo.controller;
import RokerPeru.demo.dto.RolRequestDTO;
import RokerPeru.demo.dto.RolResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.services.RolService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    @GetMapping("/listar")
    public List<RolResponseDTO> listarRol() {
        return rolService.listarRol();
    }

    @PostMapping("/crear")
    public RolResponseDTO crearRol(@RequestBody RolRequestDTO rol){
        return rolService.crearRol(rol);
    }

    @GetMapping("/buscar/{id}")
    public RolResponseDTO buscarPorId(@PathVariable Long id){
        return rolService.buscarPorId(id);
    }
}
