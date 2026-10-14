package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Proveedores;

public interface ProveedorService {
    List<Proveedores> Listar();
    Proveedores guardar(Proveedores proveedor);
    void eliminar(Long id);
}
