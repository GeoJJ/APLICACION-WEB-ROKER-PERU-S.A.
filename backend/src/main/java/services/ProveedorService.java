package services;
import entities.Proveedores;
import java.util.List;

public interface ProveedorService {
    List<Proveedores> Listar();
    Proveedores guardar(Proveedores proveedor);
    void eliminar(Long id);
}
