package RokerPeru.demo.services;
import java.util.List;

import RokerPeru.demo.entities.Ordenes_Servicio;

public interface Ordenes_ServicioService {
    List<Ordenes_Servicio> listar();
    Ordenes_Servicio guardar(Ordenes_Servicio ordenes_servicio);
    void eliminar(Long id);
}
