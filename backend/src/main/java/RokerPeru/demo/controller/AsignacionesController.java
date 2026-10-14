package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.services.AsignacionesService;

import java.util.List;

@Controller 
@RequestMapping("/asignaciones")
public class AsignacionesController {
    @Autowired 
    private AsignacionesService asignacionesService;

    @GetMapping
    public String listarAsignaciones(Model model){
        List<Asignaciones> asignaciones = asignacionesService.listar();
        model.addAttribute("listarAsignaciones", asignaciones);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAsignaciones(Model model){
        model.addAttribute("asignaciones", new Asignaciones());
        model.addAttribute("accion", "/asignaciones/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoAsignaciones(@ModelAttribute Asignaciones asignaciones){
        asignacionesService.guardar(asignaciones);
        return "redirect:/asignaciones";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarAsignaciones(@PathVariable Long id){
        asignacionesService.eliminar(id);
        return "redirect:/asignacioness";
    }

}
