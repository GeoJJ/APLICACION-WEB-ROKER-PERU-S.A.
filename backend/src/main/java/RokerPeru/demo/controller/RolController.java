package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.services.RolService;

import java.util.List;

@Controller
@RequestMapping("/roles")
public class RolController {
    @Autowired
    private RolService rolService;
    
    @GetMapping 
    public String listarServicios(Model model){
        List<Rol> roles = rolService.Listar();
        model.addAttribute("listarRoles", roles);
        return "listar";
    }

}
