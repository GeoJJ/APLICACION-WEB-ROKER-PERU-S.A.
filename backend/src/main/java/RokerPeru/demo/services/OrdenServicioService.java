package RokerPeru.demo.services;

import RokerPeru.demo.dto.ordenServicio.OrdenServicioRequestDTO;
import RokerPeru.demo.dto.ordenServicio.OrdenServicioResponseDTO;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.ServicioRepository;
import RokerPeru.demo.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenServicioService {

    private final OrdenesServicioRepository ordenesServicioRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public OrdenServicioResponseDTO crearOrdenServicio(OrdenServicioRequestDTO request) {
        log.info("Creando orden de servicio para cliente ID: {}", request.getIdCliente());

        //  Extraer el servicio y el precio real de la base de datos (Regla de seguridad)
        Servicio servicio = servicioRepository.findById(request.getIdServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + request.getIdServicio()));

        OrdenesServicio orden = new OrdenesServicio();
        orden.setFechaOrdenServicio(LocalDateTime.now());
        orden.setFechaProgramadaServicio(request.getFechaProgramadaServicio());
        orden.setEstadoServicio("PENDIENTE");
        orden.setSerieComprobanteServicio(request.getSerieComprobanteServicio());
        orden.setNumeroComprobanteServicio(request.getNumeroComprobanteServicio());
        orden.setMetodoPagoServicio(request.getMetodoPagoServicio());
        orden.setIncidenciaServicio(request.getIncidenciaServicio());
        
        // 2. Asignar el monto cobrando estrictamente lo que vale el servicio
        orden.setMontoTotalServicio(servicio.getPrecioServicio());
        orden.setServicio(servicio);

        // 3. Vincular Usuarios
        orden.setCliente(buscarUsuario(request.getIdCliente()));
        if (request.getIdVendedor() != null) {
            orden.setVendedor(buscarUsuario(request.getIdVendedor()));
        }
        if (request.getIdAuxiliar() != null) {
            orden.setAuxiliar(buscarUsuario(request.getIdAuxiliar()));
        }

        return aDto(ordenesServicioRepository.save(orden));
    }


    @Transactional
    public OrdenServicioResponseDTO confirmarOrdenServicio(Long idOrden) {
        OrdenesServicio orden = buscarOrdenPorId(idOrden);
        orden.setEstadoServicio("CONFIRMADO"); // Listo para que el Personal de Servicio lo atienda
        return aDto(ordenesServicioRepository.save(orden));
    }


    @Transactional
    public OrdenServicioResponseDTO cancelarOrdenServicio(Long idOrden) {
        OrdenesServicio orden = buscarOrdenPorId(idOrden);
        orden.setEstadoServicio("CANCELADO");
        return aDto(ordenesServicioRepository.save(orden));
    }


    public List<OrdenServicioResponseDTO> listarOrdenesServicioVendedor() {
        // El vendedor ve el historial completo de la empresa para gestionar
        return ordenesServicioRepository.findAll().stream()
                .map(this::aDto)
                .collect(Collectors.toList());
    }


    public List<OrdenServicioResponseDTO> listarOrdenesServicioCliente(Long idCliente) {
        // Uso de la consulta optimizada: el cliente SOLO ve sus propias órdenes
        return ordenesServicioRepository.findByClienteIdUsuario(idCliente).stream()
                .map(this::aDto)
                .collect(Collectors.toList());
    }


    public List<OrdenServicioResponseDTO> listarOrdenesServicioPersonal() {
        // El personal técnico solo debe ver las órdenes que están listas para ser ejecutadas
        return ordenesServicioRepository.findByEstadoServicioIn(List.of("CONFIRMADO", "EN_PROCESO")).stream()
                .map(this::aDto)
                .collect(Collectors.toList());
    }


    public OrdenServicioResponseDTO buscarOrdenServicioVendedor(Long idOrden) {
        return aDto(buscarOrdenPorId(idOrden));
    }


    public OrdenServicioResponseDTO buscarOrdenServicioCliente(Long idOrden, Long idCliente) {
        OrdenesServicio orden = buscarOrdenPorId(idOrden);
        // Filtro estricto de seguridad: Evitar que un cliente acceda a la URL de otro cliente
        if (!orden.getCliente().getIdUsuario().equals(idCliente)) {
            throw new RuntimeException("Acceso denegado: Esta orden pertenece a otro cliente");
        }
        return aDto(orden);
    }


    public OrdenServicioResponseDTO buscarOrdenServicioPersonal(Long idOrden) {
        return aDto(buscarOrdenPorId(idOrden));
    }

    // ---------------- MÉTODOS PRIVADOS AUXILIARES ----------------

    private OrdenesServicio buscarOrdenPorId(Long idOrden) {
        return ordenesServicioRepository.findById(idOrden)
                .orElseThrow(() -> new RuntimeException("Orden de Servicio no encontrada: " + idOrden));
    }

    private Usuario buscarUsuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
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