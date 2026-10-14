package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Ordenes_Servicio;
import RokerPeru.demo.repository.Ordenes_ServicioRepository;

@Service
public class Ordenes_ServicioServiceImpl implements Ordenes_ServicioService {
    @Autowired
    private Ordenes_ServicioRepository ordenes_servicioRepository;

    @Override
    public List<Ordenes_Servicio> listar() {
        return ordenes_servicioRepository.findAll();
    }

    @Override
    public Ordenes_Servicio guardar(Ordenes_Servicio ordenes_servicio) {
        return ordenes_servicioRepository.save(ordenes_servicio);
    }

    @Override
    public void eliminar(Long id) {
        ordenes_servicioRepository.deleteById(id);
    }

}
