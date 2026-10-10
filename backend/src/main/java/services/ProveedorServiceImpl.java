package services;
import entities.Proveedores;
import java.util.List;
import repository.ProveedoresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProveedorServiceImpl implements ProveedorService {
    @Autowired
    private ProveedoresRepository proveedoresRepository;

    @Override
    public List<Proveedores> Listar() {
        return proveedoresRepository.findAll();
    }

    @Override
    public Proveedores guardar(Proveedores proveedor) {
        return proveedoresRepository.save(proveedor);
    }

    @Override
    public void eliminar(Long id) {
        proveedoresRepository.deleteById(id);
    }
}
