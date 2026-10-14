package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Usuario;

public interface UsuarioService {
    List<Usuario> Listar();
    Usuario guardar(Usuario usuario);
    void eliminar(Long id);
}
