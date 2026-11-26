package RokerPeru.demo.services;

import RokerPeru.demo.logging.RegistroOperacion;
import RokerPeru.demo.dto.ServicioDTO;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.enums.TipoCodigoEnum;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final CodigoService codigoService;

    @Transactional
    public ServicioDTO.Response crearServicio(ServicioDTO.Request dto) {
        Servicio servicio = dto.toEntity();
        // El código (SRV-0001) lo asigna el backend; no viene del formulario
        servicio.setCodigoServicio(codigoService.generar(TipoCodigoEnum.SERVICIO));

        Servicio guardado = servicioRepository.save(servicio);
        RegistroOperacion.infoConfirmada(log, "Servicio creado: id={}, codigo={}, nombre={}", guardado.getIdServicio(), guardado.getCodigoServicio(), dto.nombreServicio());
        return ServicioDTO.Response.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<ServicioDTO.Response> listarServicio() {
        return servicioRepository.findAll(Sort.by("nombreServicio"))
                .stream()
                .map(ServicioDTO.Response::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicioDTO.Response buscarPorIdServicio(Long id) {
        return ServicioDTO.Response.fromEntity(obtenerServicio(id));
    }

    @Transactional
    public ServicioDTO.Response actualizarServicio(Long id, ServicioDTO.Request dto) {
        Servicio servicio = obtenerServicio(id);

        dto.applyTo(servicio);

        RegistroOperacion.infoConfirmada(log, "Servicio actualizado: id={}, nombre={}", id, dto.nombreServicio());
        return ServicioDTO.Response.fromEntity(servicio); // Hibernate hace el UPDATE
    }

    @Transactional
    public void eliminarServicio(Long id) {
        Servicio servicio = obtenerServicio(id);
        servicioRepository.delete(servicio);
        RegistroOperacion.infoConfirmada(log, "Servicio eliminado: id={}", id);
    }

    // Métodos específicos para la Vista del Cliente
    @Transactional(readOnly = true)
    public List<ServicioDTO.ClienteResponse> listarServiciosCliente() {
        return servicioRepository.findAll(Sort.by("nombreServicio"))
                .stream()
                .map(ServicioDTO.ClienteResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicioDTO.ClienteResponse> buscarServiciosCliente(String texto) {
        return servicioRepository.buscarServicio(texto.trim())
                .stream()
                .map(ServicioDTO.ClienteResponse::fromEntity)
                .toList();
    }

    // Métodos privados
    private Servicio obtenerServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
    }
}