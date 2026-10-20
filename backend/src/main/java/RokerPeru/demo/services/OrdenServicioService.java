package RokerPeru.demo.services;

import RokerPeru.demo.dto.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.OrdenServicioResponseDTO;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.ServicioRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenServicioService {

    private final OrdenesServicioRepository ordenesRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    public OrdenServicioResponseDTO crear(OrdenServicioRequestDTO dto) {
        Servicio servicio = servicioRepository.findById(dto.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + dto.getIdServicio()));

        OrdenesServicio orden = new OrdenesServicio();
        orden.setFechaOrdenServicio(LocalDateTime.now());       // lo pone el servidor
        orden.setEstadoServicio("PENDIENTE");                   // lo pone el servidor
        orden.setMontoTotalServicio(servicio.getPrecioServicio()); // el precio sale de la BD
        orden.setFechaProgramadaServicio(dto.getFechaProgramadaServicio());
        orden.setSerieComprobanteServicio(dto.getSerieComprobanteServicio());
        orden.setNumeroComprobanteServicio(dto.getNumeroComprobanteServicio());
        orden.setMetodoPagoServicio(dto.getMetodoPagoServicio());

        orden.setServicio(servicio);
        orden.setCliente(buscarUsuario(dto.getIdCliente()));
        if (dto.getIdVendedor() != null) orden.setVendedor(buscarUsuario(dto.getIdVendedor()));
        if (dto.getIdAuxiliar() != null) orden.setAuxiliar(buscarUsuario(dto.getIdAuxiliar()));

        return aDto(ordenesRepository.save(orden));
    }

    public List<OrdenServicioResponseDTO> listar() {
        return ordenesRepository.findAll().stream().map(this::aDto).toList();
    }

    public OrdenServicioResponseDTO buscarPorId(Long id) {
        return aDto(buscarOrden(id));
    }

    public OrdenServicioResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        OrdenesServicio orden = buscarOrden(id);
        orden.setEstadoServicio(nuevoEstado);
        return aDto(orden);
    }

    public OrdenServicioResponseDTO registrarIncidencia(Long id, String incidencia) {
        OrdenesServicio orden = buscarOrden(id);
        orden.setIncidenciaServicio(incidencia);
        return aDto(orden);
    }

    private OrdenesServicio buscarOrden(Long id) {
        return ordenesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de servicio no encontrada: " + id));
    }

    private Usuario buscarUsuario(Long id) {
        if (id == null) throw new RuntimeException("Falta el id del usuario");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    private OrdenServicioResponseDTO aDto(OrdenesServicio o) {
        return new OrdenServicioResponseDTO(
                o.getIdOrdenServicio(),
                o.getFechaOrdenServicio(),
                o.getFechaProgramadaServicio(),
                o.getSerieComprobanteServicio(),
                o.getNumeroComprobanteServicio(),
                o.getEstadoServicio(),
                o.getMontoTotalServicio(),
                o.getMetodoPagoServicio(),
                o.getIncidenciaServicio(),
                o.getServicio().getNombreServicio(),
                o.getCliente().getNombreUsuario(),
                o.getVendedor() != null ? o.getVendedor().getNombreUsuario() : null,
                o.getAuxiliar() != null ? o.getAuxiliar().getNombreUsuario() : null
        );
    }
}