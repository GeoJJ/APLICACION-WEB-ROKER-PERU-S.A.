package RokerPeru.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.services.ProductoService;

import java.util.List;

@Controller 
@RequestMapping("/producto")
public class ProductoController {
    @Autowired 
    private ProductoService productoService;

    @GetMapping
    public String listarProductos(Model model){
        List<Producto> productos = productoService.listar();
        model.addAttribute("listarProductos", productos);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoProducto(Model model){
        model.addAttribute("productos", new Producto());
        model.addAttribute("accion", "/producto/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoProducto(@ModelAttribute Producto producto){
        productoService.guardar(producto);
        return "redirect:/Producto";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id){
        productoService.eliminar(id);
        return "redirect:/producto";
    }
}
