package RokerPeru.demo.services;

import RokerPeru.demo.entities.EmpleadosServicio;
import RokerPeru.demo.repository.EmpleadosServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EmpleadosService {
    @Autowired
    private EmpleadosServicioRepository empleados_ServicioRepository;

    public List<EmpleadosServicio> listar() {
        return empleados_ServicioRepository.findAll();
    }

    public EmpleadosServicio guardar(EmpleadosServicio empleados_Servicio) {
        return empleados_ServicioRepository.save(empleados_Servicio);
    }

    public void eliminar(Long id) {
        empleados_ServicioRepository.deleteById(id);
    }
}
