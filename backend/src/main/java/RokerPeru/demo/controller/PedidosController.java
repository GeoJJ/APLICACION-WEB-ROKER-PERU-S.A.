package RokerPeru.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.services.PedidoService;

import java.util.List;

@Controller 
@RequestMapping("/pedidos")
public class PedidosController {
    @Autowired 
    private PedidoService pedidoService;

    @GetMapping
    public String listarPedidos(Model model){
        List<Pedidos> pedidos = pedidoService.Listar();
        model.addAttribute("listarPedidos", pedidos);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoPedido(Model model){
        model.addAttribute("pedidos", new Pedidos());
        model.addAttribute("accion", "/pedido/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoPedido(@ModelAttribute Pedidos pedido){
        pedidoService.guardar(pedido);
        return "redirect:/pedido";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id){
        pedidoService.eliminar(id);
        return "redirect:/pedido";
    }
}
