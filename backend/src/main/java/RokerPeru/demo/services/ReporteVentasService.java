package RokerPeru.demo.services;

import RokerPeru.demo.logging.RegistroOperacion;
import RokerPeru.demo.dto.VentaReporteDTO;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.exception.SolicitudInvalidaException;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.PedidosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro de ventas en PDF (Jasper) para el gerente general.
 * Un período = un mes de un año (mes 1-12) o el año completo (mes = null).
 * Se toman los comprobantes EMITIDOS en el período (no cancelados): boletas y facturas
 * de pedidos de productos y de órdenes de servicio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteVentasService {

    // Los precios del sistema llevan IGV incluido (18%), igual que en el comprobante.
    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_GENERADO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    public enum Alcance {TODOS, PRODUCTOS, SERVICIOS}

    public record ReportePdf(String nombreArchivo, byte[] contenido) {
    }

    // Fila + su fecha real, solo para ordenar productos y servicios juntos
    private record Fila(LocalDateTime fecha, VentaReporteDTO dto) {
    }

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

    private byte[] logoBytes;

    @Transactional(readOnly = true)
    public ReportePdf generar(int anio, Integer mes, Alcance alcance) {
        if (anio < 2000 || anio > LocalDate.now().getYear() + 1) {
            throw new SolicitudInvalidaException("El año indicado no es válido: " + anio);
        }
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new SolicitudInvalidaException("El mes debe estar entre 1 y 12");
        }
        Alcance filtro = alcance == null ? Alcance.TODOS : alcance;

        LocalDate inicio = LocalDate.of(anio, mes == null ? 1 : mes, 1);
        LocalDate fin = mes == null ? inicio.plusYears(1) : inicio.plusMonths(1);
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atStartOfDay();

        List<Fila> filas = new ArrayList<>();
        if (filtro != Alcance.SERVICIOS) {
            pedidosRepository.findVentasEmitidasEntre(desde, hasta)
                    .forEach(p -> filas.add(filaDePedido(p)));
        }
        if (filtro != Alcance.PRODUCTOS) {
            ordenesRepository.findVentasEmitidasEntre(desde, hasta)
                    .forEach(o -> filas.add(filaDeOrden(o)));
        }
        filas.sort(Comparator.comparing(Fila::fecha));
        List<VentaReporteDTO> detalle = filas.stream().map(Fila::dto).toList();

        // Totales (se calculan aquí para que el PDF solo los muestre)
        BigDecimal totalProductos = BigDecimal.ZERO, totalServicios = BigDecimal.ZERO;
        BigDecimal sumaSubtotal = BigDecimal.ZERO, sumaIgv = BigDecimal.ZERO;
        int cantProductos = 0, cantServicios = 0;
        for (VentaReporteDTO v : detalle) {
            sumaSubtotal = sumaSubtotal.add(v.getSubtotal());
            sumaIgv = sumaIgv.add(v.getIgv());
            if ("Producto".equals(v.getTipo())) {
                totalProductos = totalProductos.add(v.getTotal());
                cantProductos++;
            } else {
                totalServicios = totalServicios.add(v.getTotal());
                cantServicios++;
            }
        }

        String periodo = mes == null ? "Año " + anio : MESES[mes - 1] + " " + anio;

        Map<String, Object> p = new HashMap<>();
        p.put("razonSocial", razonSocial);
        p.put("rucEmpresa", ruc);
        p.put("direccionEmpresa", direccion);
        p.put("logo", abrirLogo());
        p.put("periodo", periodo);
        p.put("alcance", switch (filtro) {
            case TODOS -> "Productos y servicios";
            case PRODUCTOS -> "Solo productos";
            case SERVICIOS -> "Solo servicios";
        });
        p.put("fechaGeneracion", LocalDateTime.now().format(FMT_GENERADO));
        p.put("generadoPor", usuarioActual());
        p.put("cantidadComprobantes", detalle.size());
        p.put("cantidadProductos", cantProductos);
        p.put("cantidadServicios", cantServicios);
        p.put("totalSubtotal", sumaSubtotal);
        p.put("totalIgv", sumaIgv);
        p.put("totalProductos", totalProductos);
        p.put("totalServicios", totalServicios);
        p.put("totalGeneral", totalProductos.add(totalServicios));

        RegistroOperacion.infoConfirmada(log, "AUDITORÍA: registro de ventas generado ({}, {}): {} comprobantes", periodo, filtro, detalle.size());

        String nombre = "registro-ventas-" + anio + (mes == null ? "" : "-" + String.format("%02d", mes)) + ".pdf";
        return new ReportePdf(nombre, reporteService.generarPdf("registro_ventas", p, detalle));
    }

    // Filas

    private Fila filaDePedido(Pedidos pedido) {
        Usuario cliente = pedido.getCliente();
        String nombre;
        String documento;
        if (cliente != null) {
            nombre = textoODash(cliente.getNombreUsuario());
            documento = documentoDe(cliente);
        } else { // venta presencial: solo se conoce el DNI
            nombre = "Cliente de mostrador";
            documento = pedido.getClientePresencial() != null
                    ? "DNI " + pedido.getClientePresencial().getDniClientePresencial() : "-";
        }
        return fila(pedido.getFechaEmisionComprobante(),
                pedido.getSerieComprobantePedido() + "-" + pedido.getNumeroComprobantePedido(),
                "Producto", nombre, documento, pedido.getVendedor(),
                pedido.getMetodoPagoPedido(), pedido.getMontoTotalPedido());
    }

    private Fila filaDeOrden(OrdenesServicio orden) {
        Usuario cliente = orden.getCliente();
        return fila(orden.getFechaEmisionComprobante(),
                orden.getSerieComprobanteServicio() + "-" + orden.getNumeroComprobanteServicio(),
                "Servicio", textoODash(cliente.getNombreUsuario()), documentoDe(cliente), orden.getVendedor(),
                orden.getMetodoPagoServicio(), orden.getMontoTotalServicio());
    }

    private Fila fila(LocalDateTime fecha, String comprobante, String tipo, String cliente, String documento,
                      Usuario vendedor, String metodoPago, double monto) {
        BigDecimal total = BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);
        VentaReporteDTO dto = new VentaReporteDTO(
                fecha != null ? fecha.format(FMT_FECHA) : "-",
                comprobante, tipo, cliente, documento,
                vendedor != null ? textoODash(vendedor.getNombreUsuario()) : "-",
                textoODash(metodoPago), subtotal, igv, total);
        return new Fila(fecha != null ? fecha : LocalDateTime.MIN, dto);
    }

    // Utilidades

    private String documentoDe(Usuario cliente) {
        String doc = cliente.getNumeroDocumentoUsuario();
        if (doc == null || doc.isBlank()) return "-";
        boolean empresa = "JURIDICA".equalsIgnoreCase(cliente.getTipoPersonaUsuario());
        return (empresa ? "RUC " : doc.length() == 8 ? "DNI " : "DOC ") + doc;
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Usuario u ? textoODash(u.getNombreUsuario()) : "-";
    }

    private String textoODash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private synchronized InputStream abrirLogo() {
        if (logoBytes == null) {
            try (InputStream in = new ClassPathResource(logoRuta).getInputStream()) {
                logoBytes = in.readAllBytes();
            } catch (IOException e) {
                log.warn("No se encontró el logo '{}': el reporte saldrá sin logo", logoRuta);
                logoBytes = new byte[0];
            }
        }
        return logoBytes.length == 0 ? null : new ByteArrayInputStream(logoBytes);
    }
}