package RokerPeru.demo.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Empleados_Servicio;
import RokerPeru.demo.repository.Empleados_ServicioRepository;

@Service 
public class EmpleadosServiceImpl implements EmpleadosService{
    @Autowired
    private Empleados_ServicioRepository empleados_ServicioRepository;

    @Override
    public List<Empleados_Servicio> listar() {
        return empleados_ServicioRepository.findAll();
    }

    @Override
    public Empleados_Servicio guardar(Empleados_Servicio empleados_Servicio) {
        return empleados_ServicioRepository.save(empleados_Servicio);
    }

    @Override
    public void eliminar(Long id) {
        empleados_ServicioRepository.deleteById(id);
    }
}
