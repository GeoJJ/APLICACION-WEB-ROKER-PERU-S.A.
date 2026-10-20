package RokerPeru.demo.controller;
import RokerPeru.demo.dto.RolResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.services.RolService;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {
    @Autowired
    private RolService rolService;
    @GetMapping
    public List<RolResponseDTO> listar() {
        return rolService.listarRol();
    }
}
