package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Empleados_Servicio;

public interface EmpleadosService {
    List<Empleados_Servicio> listar();
    Empleados_Servicio guardar(Empleados_Servicio empleados_servicio);
    void eliminar(Long id);
}
