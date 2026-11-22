package RokerPeru.demo.services;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoCodigoEnum;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.exception.ReglaNegocioException;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.ServicioRepository;
import RokerPeru.demo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenServicioService {

    private final OrdenesServicioRepository ordenesRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComprobanteService comprobanteService;
    private final CodigoService codigoService;

    // Flujo del cliente para crear orden de servicio
    @Transactional
    public OrdenServicioDTO.ResponseCliente crearOrdenServicio(OrdenServicioDTO.RequestCliente dto, Long idCliente) {
        Servicio servicio = servicioRepository.findById(dto.idServicio())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", dto.idServicio()));

        Usuario cliente = obtenerUsuario(idCliente);
        OrdenesServicio orden = dto.toEntity(servicio, cliente);
        orden.setFechaOrdenServicio(LocalDateTime.now());
        orden.setEstadoServicio("PENDIENTE");
        orden.setMontoTotalServicio(servicio.getPrecioServicio());
        // El código (ORD-0001) lo asigna el backend; no viene del formulario
        orden.setCodigoOrdenServicio(codigoService.generar(TipoCodigoEnum.ORDEN_SERVICIO));

        OrdenesServicio guardado = ordenesRepository.save(orden);
        log.info("Cliente {} creó la orden de servicio {} (pendiente de confirmación) para {}",
                cliente.getNombreUsuario(), guardado.getCodigoOrdenServicio(), servicio.getNombreServicio());

        return OrdenServicioDTO.ResponseCliente.fromEntity(guardado);
    }

    @Transactional
    public OrdenServicioDTO.ResponseVendedor confirmarOrdenServicio(Long idOrden, OrdenServicioDTO.RequestGestionVendedor dto, Long idVendedor) {
        // Traemos la orden CON sus asignaciones (gracias al EntityGraph del repositorio)
        OrdenesServicio orden = obtenerOrdenConDetalles(idOrden);

        String estadoActual = orden.getEstadoServicio();
        if (!"PENDIENTE".equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden confirmar órdenes PENDIENTES. Estado actual: " + estadoActual);
        }
        int personalRequerido = 0;
        try {
            String soloNumeros = orden.getServicio().getCantidadPersonalServicio().replaceAll("\\D+", "");
            personalRequerido = Integer.parseInt(soloNumeros);
        } catch (Exception e) {
            personalRequerido = 1;
        }

        int personalAsignado = orden.getAsignaciones() != null ? orden.getAsignaciones().size() : 0;

        if (personalAsignado < personalRequerido) {
            throw new ReglaNegocioException(String.format(
                    "No se puede confirmar la orden. El servicio requiere %d personas, pero solo se han asignado %d.",
                    personalRequerido, personalAsignado
            ));
        }

        orden.setVendedor(obtenerUsuario(idVendedor));
        // Si pasa la validación, aplicamos los cambios y confirmamos
        dto.applyTo(orden); // Esto cambia el estado a "CONFIRMADO" y agrega incidencias si hay
        emitirComprobante(orden);
        OrdenesServicio actualizado = ordenesRepository.save(orden);

        log.info("Vendedor confirmó la orden de servicio #{}. Personal asignado: {}/{}",
                idOrden, personalAsignado, personalRequerido);

        return OrdenServicioDTO.ResponseVendedor.fromEntity(actualizado);
    }


    // Flujo del vendedor para cancelar orden
    @Transactional
    public OrdenServicioDTO.ResponseVendedor cancelarOrdenServicio(Long idOrden, OrdenServicioDTO.RequestGestionVendedor dto, Long idVendedor) {
        OrdenesServicio orden = obtenerOrden(idOrden);

        if ("COMPLETADO".equals(orden.getEstadoServicio())) {
            throw new OperacionNoPermitidaException("No se puede cancelar una orden ya completada");
        }
        if ("CANCELADO".equals(orden.getEstadoServicio())) {
            throw new OperacionNoPermitidaException("La orden ya está cancelada");
        }

        // Si nadie la había tomado todavía, queda a nombre de quien la cancela
        // (así sigue apareciendo en su lista y se sabe quién la atendió).
        if (orden.getVendedor() == null) {
            orden.setVendedor(obtenerUsuario(idVendedor));
        }

        dto.applyTo(orden);
        OrdenesServicio actualizado = ordenesRepository.save(orden);

        log.info("Orden de servicio #{} cancelada", idOrden);
        return OrdenServicioDTO.ResponseVendedor.fromEntity(actualizado);
    }

    @Transactional(readOnly = true)
    public List<OrdenServicioDTO.ResponseCliente> listarOrdenesServicioCliente(Long idCliente) {
        return ordenesRepository.findByCliente_IdUsuarioOrderByFechaOrdenServicioDesc(idCliente)
                .stream()
                .map(OrdenServicioDTO.ResponseCliente::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenServicioDTO.ResponseVendedor> listarOrdenesServicioVendedor() {
        return ordenesRepository.findAllByOrderByFechaOrdenServicioDesc()
                .stream()
                .map(OrdenServicioDTO.ResponseVendedor::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenServicioDTO.ResponseVendedor> listarOrdenesServicioPersonal() {
        List<String> estadosActivos = List.of("CONFIRMADO", "EN_PROCESO");
        return ordenesRepository.findByEstadoServicioInOrderByFechaOrdenServicioDesc(estadosActivos)
                .stream()
                .map(OrdenServicioDTO.ResponseVendedor::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenServicioDTO.ResponseCliente buscarOrdenServicioCliente(Long idOrden, Long idCliente) {
        validarAccesoCliente(idOrden, idCliente);
        return OrdenServicioDTO.ResponseCliente.fromEntity(obtenerOrden(idOrden));
    }

    @Transactional(readOnly = true)
    public OrdenServicioDTO.ResponseVendedor buscarOrdenServicioVendedor(Long idOrden) {
        return OrdenServicioDTO.ResponseVendedor.fromEntity(obtenerOrden(idOrden));
    }

    private OrdenesServicio obtenerOrden(Long id) {
        return ordenesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio", id));
    }

    private OrdenesServicio obtenerOrdenConDetalles(Long id) {
        return ordenesRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio", id));
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private void validarAccesoCliente(Long idOrden, Long idCliente) {
        boolean pertenece = ordenesRepository.existsByIdOrdenServicioAndCliente_IdUsuario(idOrden, idCliente);
        if (!pertenece) {
            throw new OperacionNoPermitidaException("Acceso denegado: Esta orden no pertenece a este cliente");
        }
    }

    private void emitirComprobante(OrdenesServicio orden) {
        if (orden.getNumeroComprobanteServicio() != null) {
            return;
        }
        TipoComprobanteEnum tipo = ComprobanteService.resolverTipo(orden.getCliente(), true);
        ComprobanteService.NumeroComprobante numero = comprobanteService.emitir(tipo);

        orden.setSerieComprobanteServicio(numero.serie());
        orden.setNumeroComprobanteServicio(numero.numero());
        orden.setFechaEmisionComprobante(LocalDateTime.now());
    }

    @Transactional
    public OrdenServicioDTO.ResponseVendedor completarOrdenServicio(Long idOrden, OrdenServicioDTO.RequestGestionVendedor dto) {
        OrdenesServicio orden = obtenerOrden(idOrden);

        String estadoActual = orden.getEstadoServicio();
        if (!"CONFIRMADO".equals(estadoActual) && !"EN_PROCESO".equals(estadoActual)) {
            throw new OperacionNoPermitidaException(
                    "Solo se pueden completar órdenes CONFIRMADAS. Estado actual: " + estadoActual);
        }

        dto.applyTo(orden); // estado = COMPLETADO y, si viene, la observación
        OrdenesServicio actualizado = ordenesRepository.save(orden);

        log.info("Orden de servicio #{} completada", idOrden);
        return OrdenServicioDTO.ResponseVendedor.fromEntity(actualizado);
    }
}