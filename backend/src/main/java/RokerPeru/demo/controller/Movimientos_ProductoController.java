package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Movimientos_Producto;
import RokerPeru.demo.services.Movimientos_ProductoService;

import java.util.List;

@Controller 
@RequestMapping("/movimientos_Producto")
public class Movimientos_ProductoController {
    @Autowired 
    private Movimientos_ProductoService movimientos_ProductoService;

    @GetMapping
    public String listarMovimientosProducto(Model model){
        List<Movimientos_Producto> movimientosServicios= movimientos_ProductoService.listar();
        model.addAttribute("listarMovimientos_Producto", movimientosServicios);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioMovimientoProducto(Model model){
        model.addAttribute("movimientos_Servicios", new Movimientos_Producto());
        model.addAttribute("accion", "/movimientos_Producto/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoMovimientoProducto(@ModelAttribute Movimientos_Producto movimientos_Producto){
        movimientos_ProductoService.guardar(movimientos_Producto);
        return "redirect:/movimientos_producto";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarMovimientoProducto(@PathVariable Long id){
        movimientos_ProductoService.eliminar(id);
        return "redirect:/movimientos_producto";
    }
}
