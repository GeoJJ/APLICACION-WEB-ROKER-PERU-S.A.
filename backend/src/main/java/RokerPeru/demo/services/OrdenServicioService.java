package RokerPeru.demo.services;

import RokerPeru.demo.dto.OrdenServicioDTO;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
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

    // Flujo del cliente para crear orden de servicio
    @Transactional
    public OrdenServicioDTO.ResponseCliente crearOrdenServicio(OrdenServicioDTO.RequestCliente dto) {
        Servicio servicio = servicioRepository.findById(dto.idServicio())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + dto.idServicio()));

        Usuario cliente = obtenerUsuario(dto.idCliente());

        OrdenesServicio orden = dto.toEntity(servicio, cliente);
        orden.setFechaOrdenServicio(LocalDateTime.now());
        orden.setEstadoServicio("PENDIENTE");
        orden.setMontoTotalServicio(servicio.getPrecioServicio());

        TipoComprobanteEnum tipoComprobante;

        // Si es empresa (JURIDICA), le damos Factura de Servicio. Si es persona (NATURAL), Boleta de Servicio.
        if ("JURIDICA".equals(cliente.getTipoPersonaUsuario())) {
            tipoComprobante = TipoComprobanteEnum.FACTURA_SERVICIO;
        } else {
            tipoComprobante = TipoComprobanteEnum.BOLETA_SERVICIO;
        }

        // Generamos el número automático (ej: "B002-00000001" o "F002-00000001")
        String numeroGenerado = comprobanteService.generarNumeroComprobante(tipoComprobante);

        // Asignamos a la entidad
        orden.setSerieComprobanteServicio(tipoComprobante.getSerie());
        orden.setNumeroComprobanteServicio(numeroGenerado);

        OrdenesServicio guardado = ordenesRepository.save(orden);
        log.info("Cliente {} creó orden de servicio #{} con comprobante {} para {}",
                cliente.getNombreUsuario(), guardado.getIdOrdenServicio(), numeroGenerado, servicio.getNombreServicio());

        return OrdenServicioDTO.ResponseCliente.fromEntity(guardado);
    }

    @Transactional
    public OrdenServicioDTO.ResponseVendedor confirmarOrdenServicio(Long idOrden, OrdenServicioDTO.RequestGestionVendedor dto) {
        // Traemos la orden CON sus asignaciones (gracias al EntityGraph del repositorio)
        OrdenesServicio orden = obtenerOrdenConDetalles(idOrden);

        if (!"PENDIENTE".equals(orden.getEstadoServicio())) {
            throw new OperacionNoPermitidaException("Solo se pueden confirmar órdenes en estado PENDIENTE");
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

        // Si pasa la validación, aplicamos los cambios y confirmamos
        dto.applyTo(orden); // Esto cambia el estado a "CONFIRMADO" y agrega incidencias si hay

        OrdenesServicio actualizado = ordenesRepository.save(orden);

        log.info("Vendedor confirmó la orden de servicio #{}. Personal asignado: {}/{}",
                idOrden, personalAsignado, personalRequerido);

        return OrdenServicioDTO.ResponseVendedor.fromEntity(actualizado);
    }


    // Flujo del vendedor para cancelar orden
    @Transactional
    public OrdenServicioDTO.ResponseVendedor cancelarOrdenServicio(Long idOrden, OrdenServicioDTO.RequestGestionVendedor dto) {
        OrdenesServicio orden = obtenerOrden(idOrden);

        if ("COMPLETADO".equals(orden.getEstadoServicio())) {
            throw new OperacionNoPermitidaException("No se puede cancelar una orden ya completada");
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }

    private void validarAccesoCliente(Long idOrden, Long idCliente) {
        boolean pertenece = ordenesRepository.existsByIdOrdenServicioAndCliente_IdUsuario(idOrden, idCliente);
        if (!pertenece) {
            throw new OperacionNoPermitidaException("Acceso denegado: Esta orden no pertenece a este cliente");
        }
    }
}