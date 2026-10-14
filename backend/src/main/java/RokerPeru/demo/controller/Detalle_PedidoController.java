package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Detalle_Pedido;
import RokerPeru.demo.services.Detalle_PedidoService;

import java.util.List;

@Controller 
@RequestMapping("/detalle_pedido")
public class Detalle_PedidoController {
    
    @Autowired 
    private Detalle_PedidoService detalle_PedidoService;

    @GetMapping
    public String listarMovimientosProducto(Model model){
        List<Detalle_Pedido> detalle_Pedidos= detalle_PedidoService.listar();
        model.addAttribute("listarDetalle_Pedido", detalle_Pedidos);
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioDetallePedido(Model model){
        model.addAttribute("detalle_Pedido", new Detalle_Pedido());
        model.addAttribute("accion", "/detalle_Pedido/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoDetallePedido(@ModelAttribute Detalle_Pedido detalle_Pedido){
        detalle_PedidoService.guardar(detalle_Pedido);
        return "redirect:/detalle_Pedido";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarDetallePedidos(@PathVariable Long id){
        detalle_PedidoService.eliminar(id);
        return "redirect:/detalle_Pedido";
    }

}
