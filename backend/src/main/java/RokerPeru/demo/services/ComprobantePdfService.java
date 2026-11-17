package RokerPeru.demo.services;

import RokerPeru.demo.dto.ComprobanteDTO;
import RokerPeru.demo.entities.DetallePedido;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.OperacionNoPermitidaException;
import RokerPeru.demo.exception.RecursoNoEncontradoException;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.PedidosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComprobantePdfService {

    // Los precios del sistema se consideran CON IGV incluido (18%).
    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");

    public record ComprobantePdf(String nombreArchivo, byte[] contenido) {}

    private final PedidosRepository pedidosRepository;
    private final OrdenesServicioRepository ordenesRepository;
    private final ReporteService reporteService;

    @Value("${empresa.razon-social}")
    private String razonSocial;
    @Value("${empresa.ruc}")
    private String ruc;
    @Value("${empresa.direccion}")
    private String direccion;
    @Value("${empresa.logo:reports/logo.png}")
    private String logoRuta;

    private byte[] logoBytes; // se lee una sola vez

    // ---------------------------------------------------------------- PEDIDOS (productos)

    @Transactional(readOnly = true)
    public ComprobantePdf generarDePedido(Long idPedido, Usuario solicitante) {
        Pedidos pedido = pedidosRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido ", idPedido));

        validarAccesoPedido(pedido, solicitante);

        if (pedido.getNumeroComprobantePedido() == null) {
            throw new OperacionNoPermitidaException(
                    "El pedido aún no tiene comprobante: se emite cuando el vendedor lo confirma");
        }

        List<ComprobanteDTO> filas = pedido.getDetallePedidos().stream()
                .map(this::filaDePedido)
                .toList();

        Usuario cliente = pedido.getCliente();
        String serie = pedido.getSerieComprobantePedido();
        String numero = serie + "-" + pedido.getNumeroComprobantePedido();

        Map<String, Object> p = parametrosBase(cliente, serie, numero,
                pedido.getFechaEmisionComprobante(),
                BigDecimal.valueOf(pedido.getMontoTotalPedido()));
        p.put("tipoOperacion", "Venta de productos");
        p.put("referencia", "Pedido N° " + pedido.getIdPedido());
        p.put("formaPago", textoODash(pedido.getMetodoPagoPedido()));
        p.put("atendidoPor", pedido.getVendedor() != null ? pedido.getVendedor().getNombreUsuario() : "-");

        return new ComprobantePdf(nombreArchivo(numero), reporteService.generarPdf("comprobante", p, filas));
    }

    // ---------------------------------------------------------------- ÓRDENES DE SERVICIO

    @Transactional(readOnly = true)
    public ComprobantePdf generarDeOrdenServicio(Long idOrden, Usuario solicitante) {
        OrdenesServicio orden = ordenesRepository.findById(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden de servicio ", idOrden));

        validarAccesoOrden(orden, solicitante);

        if (orden.getNumeroComprobanteServicio() == null) {
            throw new OperacionNoPermitidaException(
                    "La orden aún no tiene comprobante: se emite cuando el vendedor la confirma");
        }

        BigDecimal monto = BigDecimal.valueOf(orden.getMontoTotalServicio()).setScale(2, RoundingMode.HALF_UP);
        String descripcion = orden.getServicio().getNombreServicio();
        List<ComprobanteDTO> filas = List.of(new ComprobanteDTO(descripcion, BigDecimal.ONE, monto, monto));

        Usuario cliente = orden.getCliente();
        String serie = orden.getSerieComprobanteServicio();
        String numero = serie + "-" + orden.getNumeroComprobanteServicio();

        Map<String, Object> p = parametrosBase(cliente, serie, numero,
                orden.getFechaEmisionComprobante(), monto);
        p.put("tipoOperacion", "Prestación de servicios");
        p.put("referencia", "Orden de servicio N° " + orden.getIdOrdenServicio());
        p.put("formaPago", textoODash(orden.getMetodoPagoServicio()));
        p.put("atendidoPor", orden.getVendedor() != null ? orden.getVendedor().getNombreUsuario() : "-");

        return new ComprobantePdf(nombreArchivo(numero), reporteService.generarPdf("comprobante", p, filas));
    }

    // ---------------------------------------------------------------- permisos

    // ADMIN y GERENTE ven todo. VENDEDOR solo los pedidos que él confirmó. CLIENTE solo los suyos.
    private void validarAccesoPedido(Pedidos pedido, Usuario u) {
        switch (rolDe(u)) {
            case "ADMINISTRADOR", "GERENTE_GENERAL" -> { }
            case "VENDEDOR" -> {
                boolean esSuVenta = pedido.getVendedor() != null
                        && pedido.getVendedor().getIdUsuario().equals(u.getIdUsuario());
                if (!esSuVenta) throw denegado();
            }
            case "CLIENTE" -> {
                if (!pedido.getCliente().getIdUsuario().equals(u.getIdUsuario())) throw denegado();
            }
            default -> throw denegado();
        }
    }

    // En servicios el vendedor ya ve todas las órdenes, por eso aquí también.
    private void validarAccesoOrden(OrdenesServicio orden, Usuario u) {
        switch (rolDe(u)) {
            case "ADMINISTRADOR", "GERENTE_GENERAL", "VENDEDOR" -> { }
            case "CLIENTE" -> {
                if (!orden.getCliente().getIdUsuario().equals(u.getIdUsuario())) throw denegado();
            }
            default -> throw denegado();
        }
    }

    private String rolDe(Usuario u) {
        return u.getRol() != null ? u.getRol().getNombreRol() : "";
    }

    private OperacionNoPermitidaException denegado() {
        return new OperacionNoPermitidaException("Acceso denegado: no puedes ver este comprobante");
    }

    // ---------------------------------------------------------------- parámetros del reporte

    private Map<String, Object> parametrosBase(Usuario cliente, String serie, String numeroCompleto,
                                               LocalDateTime fechaEmision, BigDecimal montoTotal) {
        BigDecimal total = montoTotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);

        boolean factura = serie != null && serie.toUpperCase().startsWith("F");
        boolean esEmpresa = "JURIDICA".equalsIgnoreCase(cliente.getTipoPersonaUsuario());
        String doc = cliente.getNumeroDocumentoUsuario();
        String tipoDoc = esEmpresa ? "RUC" : (doc != null && doc.length() == 8 ? "DNI" : "DOC");

        Map<String, Object> p = new HashMap<>();
        // Empresa
        p.put("razonSocial", razonSocial);
        p.put("rucEmpresa", ruc);
        p.put("direccionEmpresa", direccion);
        p.put("logo", abrirLogo()); // null si no existe: el PDF sale sin logo
        // Comprobante
        p.put("tipoComprobante", factura ? "FACTURA" : "BOLETA DE VENTA");
        p.put("numeroComprobante", numeroCompleto);
        p.put("fechaEmision", fechaEmision != null
                ? Date.from(fechaEmision.atZone(ZoneId.systemDefault()).toInstant()) : new Date());
        // Cliente
        p.put("clienteNombre", textoODash(cliente.getNombreUsuario()));
        p.put("clienteDocumentoTipo", tipoDoc);
        p.put("clienteDocumentoNumero", textoODash(doc));
        p.put("clienteDireccion", textoODash(cliente.getDireccionUsuario()));
        // Totales
        p.put("subtotal", subtotal);
        p.put("impuestoMonto", igv);
        p.put("total", total);
        p.put(net.sf.jasperreports.engine.JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
        return p;
    }

    private ComprobanteDTO filaDePedido(DetallePedido d) {
        return new ComprobanteDTO(
                d.getProducto().getNombreProducto(),
                BigDecimal.valueOf(d.getCantidadProducto()),
                BigDecimal.valueOf(d.getPrecioUnitarioProducto()).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(d.getSubtotal()).setScale(2, RoundingMode.HALF_UP));
    }

    // ---------------------------------------------------------------- utilidades

    private synchronized InputStream abrirLogo() {
        if (logoBytes == null) {
            try (InputStream in = new ClassPathResource(logoRuta).getInputStream()) {
                logoBytes = in.readAllBytes();
            } catch (IOException e) {
                log.warn("No se encontró el logo '{}': el comprobante saldrá sin logo", logoRuta);
                logoBytes = new byte[0];
            }
        }
        // Un InputStream nuevo por cada PDF (solo se puede leer una vez)
        return logoBytes.length == 0 ? null : new ByteArrayInputStream(logoBytes);
    }

    private String textoODash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private String nombreArchivo(String numeroCompleto) {
        return "comprobante-" + numeroCompleto + ".pdf";
    }
}