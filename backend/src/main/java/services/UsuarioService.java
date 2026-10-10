package services;
import entities.Usuario;
import java.util.List;

public interface UsuarioService {
    List<Usuario> Listar();
    Usuario guardar(Usuario usuario);
    void eliminar(Long id);
}
