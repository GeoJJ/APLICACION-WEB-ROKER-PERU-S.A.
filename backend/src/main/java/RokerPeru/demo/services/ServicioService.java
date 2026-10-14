package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Servicio;

public interface ServicioService {
    List<Servicio> listar();
    Servicio guardar(Servicio servicio);
    void eliminar(Long id);
}
