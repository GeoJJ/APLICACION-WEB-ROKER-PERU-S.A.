package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.services.ProveedorService;

import java.util.List;

@Controller
@RequestMapping("/proveedores")
public class ProveedoresController {
    @Autowired
    private ProveedorService proveedorService;
    
    @GetMapping 
    public String listarServicios(Model model){
        List<Proveedores> proveedores = proveedorService.Listar();
        model.addAttribute("listarProveedores", proveedores);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProveedor(Model model){
        model.addAttribute("proveedores", new Proveedores());
        model.addAttribute("accion", "/proveedores/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoProveedor(@ModelAttribute Proveedores proveedores){
        proveedorService.guardar(proveedores);
        return "redirect:/Proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id){
        proveedorService.eliminar(id);
        return "redirect:/proveedores";
    }
}
