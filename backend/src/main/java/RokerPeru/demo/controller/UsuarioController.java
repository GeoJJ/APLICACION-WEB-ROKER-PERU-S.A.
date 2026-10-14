package RokerPeru.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.services.RolService;
import RokerPeru.demo.services.UsuarioService;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
    @Autowired 
    private RolService rolService;

    @GetMapping 
    public String listarUsuarios(Model model){
        List<Usuario> usuarios = usuarioService.Listar();
        model.addAttribute("listarUsuarios", usuarios);
        model.addAttribute("roles", rolService.Listar());
        return "listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model){
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("accion", "/usuarios/nuevo");
        return "formulario";
    }

    @PostMapping("/nuevo")
    public String guardarNuevoUsuario(@ModelAttribute Usuario usuario){
        usuarioService.guardar(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id){
        usuarioService.eliminar(id);
        return "redirect:/usuarios";
    }
}
