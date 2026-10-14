package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Empleados_Servicio;
import RokerPeru.demo.services.EmpleadosService;

import java.util.List;

@Controller 
@RequestMapping("/empleados_Servicio")
public class Empleados_ServicioController {
    @Autowired 
    private EmpleadosService EmpleadosService;

    @GetMapping
    public String listarEmpleados(Model model){
        List<Empleados_Servicio> empleados_Servicios= EmpleadosService.listar();
        model.addAttribute("listarEmpleados_Servicio", empleados_Servicios);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioEmpleadosServicio(Model model){
        model.addAttribute("empleados_Servicios", new Empleados_Servicio());
        model.addAttribute("accion", "/empleados_Service/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoEmpleadoServicio(@ModelAttribute Empleados_Servicio empleados_Servicio){
        EmpleadosService.guardar(empleados_Servicio);
        return "redirect:/empleados_Service";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleadoServicio(@PathVariable Long id){
        EmpleadosService.eliminar(id);
        return "redirect:/empleados_Service";
    }
}
