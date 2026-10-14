package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Asignaciones;
import RokerPeru.demo.repository.AsignacionesRepository;

@Service 
public class AsignacionesServiceImpl implements AsignacionesService{

    @Autowired
    private AsignacionesRepository asignacionesRepository;

    @Override
    public List<Asignaciones> listar() {
        return asignacionesRepository.findAll();
    }

    @Override
    public Asignaciones guardar(Asignaciones asignaciones) {
        return asignacionesRepository.save(asignaciones);
    }

    @Override
    public void eliminar(Long id) {
        asignacionesRepository.deleteById(id);
    }

}