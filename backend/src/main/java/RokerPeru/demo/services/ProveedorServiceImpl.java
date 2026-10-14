package RokerPeru.demo.services;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Proveedores;
import RokerPeru.demo.repository.ProveedoresRepository;

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
