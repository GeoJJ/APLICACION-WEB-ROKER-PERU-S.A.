package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.services.ServicioService;

import java.util.List;

@Controller
@RequestMapping("/servicios")
public class ServicioController {
    @Autowired
    private ServicioService servicioService;
    
    @GetMapping 
    public String listarServicios(Model model){
        List<Servicio> servicios = servicioService.listar();
        model.addAttribute("listarServicios", servicios);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoServicio(Model model){
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("accion", "/servicio/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoUsuario(@ModelAttribute Servicio servicio){
        servicioService.guardar(servicio);
        return "redirect:/Servicios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id){
        servicioService.eliminar(id);
        return "redirect:/servicios";
    }
    
}
