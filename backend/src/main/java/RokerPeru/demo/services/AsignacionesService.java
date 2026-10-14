package RokerPeru.demo.services;

import java.util.List;

import RokerPeru.demo.entities.Asignaciones;

public interface AsignacionesService {
    List<Asignaciones> listar();
    Asignaciones guardar(Asignaciones asignaciones);
    void eliminar(Long id);
}
