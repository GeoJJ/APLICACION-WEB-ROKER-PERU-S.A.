package RokerPeru.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Ordenes_Servicio;
import RokerPeru.demo.services.Ordenes_ServicioService;

import java.util.List;

@Controller 
@RequestMapping("/ordenes_Servicio")
public class Ordenes_ServicioController {
    @Autowired 
    private Ordenes_ServicioService ordenes_ServicioService;

    @GetMapping
    public String listarOrdenesServicio(Model model){
        List<Ordenes_Servicio> ordenes_Servicios = ordenes_ServicioService.listar();
        model.addAttribute("listarOrdenes_Servicio", ordenes_Servicios);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioOrdenesServicios(Model model){
        model.addAttribute("ordenes_Servicios", new Ordenes_Servicio());
        model.addAttribute("accion", "/ordenes_Servicios/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoOrdenesServicio(@ModelAttribute Ordenes_Servicio ordenes_Servicio){
        ordenes_ServicioService.guardar(ordenes_Servicio);
        return "redirect:/ordenes_Servicio";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarOrdenesServicio(@PathVariable Long id){
        ordenes_ServicioService.eliminar(id);
        return "redirect:/ordenes_Servicio";
    }
}
