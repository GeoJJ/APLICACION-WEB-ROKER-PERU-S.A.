package RokerPeru.demo.dto;

import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class OrdenServicioDTO {
    private OrdenServicioDTO() {
    }

    public record RequestCliente(
            @NotNull(message = "El ID del servicio es obligatorio")
            Long idServicio,

            @NotNull(message = "La fecha programada es obligatoria")
            @Future(message = "La fecha programada debe ser futura")
            LocalDateTime fechaProgramadaServicio,

            @NotBlank(message = "El método de pago es obligatorio")
            @Pattern(regexp = "EFECTIVO|YAPE|PLIN|TARJETA|TRANSFERENCIA",
                    message = "El método de pago debe ser EFECTIVO, YAPE, PLIN, TARJETA o TRANSFERENCIA")
            String metodoPagoServicio,

            @NotBlank(message = "La dirección del servicio es obligatoria")
            @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
            String direccionServicio,

            @Size(max = 3000, message = "El detalle no puede superar los 3000 caracteres")
            String detalleSolicitudServicio
    ) {
        public RequestCliente {
            metodoPagoServicio = metodoPagoServicio != null ? metodoPagoServicio.trim().toUpperCase() : null;
            direccionServicio = direccionServicio != null ? direccionServicio.trim() : null;
            detalleSolicitudServicio = (detalleSolicitudServicio == null || detalleSolicitudServicio.isBlank())
                    ? null : detalleSolicitudServicio.trim();
        }

        public OrdenesServicio toEntity(Servicio servicio, Usuario cliente) {
            OrdenesServicio orden = new OrdenesServicio();
            applyTo(orden, servicio, cliente, null, null);
            return orden;
        }

        public void applyTo(OrdenesServicio orden, Servicio servicio, Usuario cliente, Usuario vendedor, Usuario auxiliar) {
            orden.setServicio(servicio);
            orden.setCliente(cliente);
            if (vendedor != null) orden.setVendedor(vendedor);
            if (auxiliar != null) orden.setAuxiliar(auxiliar);

            orden.setFechaProgramadaServicio(this.fechaProgramadaServicio);
            orden.setMetodoPagoServicio(this.metodoPagoServicio);
            orden.setDireccionServicio(this.direccionServicio);
            orden.setDetalleSolicitudServicio(this.detalleSolicitudServicio);
        }
    }

    public record RequestGestionVendedor(
            @NotBlank(message = "El estado es obligatorio")
            String nuevoEstado,

            String incidenciaServicio
    ) {
        public RequestGestionVendedor {
            nuevoEstado = nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : null;
            incidenciaServicio = incidenciaServicio != null ? incidenciaServicio.trim() : null;
        }

        public void applyTo(OrdenesServicio orden) {
            orden.setEstadoServicio(this.nuevoEstado);
            if (this.incidenciaServicio != null && !this.incidenciaServicio.isBlank()) {
                orden.setIncidenciaServicio(this.incidenciaServicio);
            }
        }
    }

    public record ResponseCliente(
            Long idOrdenServicio,
            String codigoOrdenServicio,
            LocalDateTime fechaOrdenServicio,
            LocalDateTime fechaProgramadaServicio,
            String serieComprobanteServicio,
            String numeroComprobanteServicio,
            LocalDateTime fechaEmisionComprobante,
            String estadoServicio,
            double montoTotalServicio,
            String metodoPagoServicio,
            String incidenciaServicio,
            String nombreServicio,
            String nombreVendedor,
            String nombreAuxiliar,
            String direccionServicio,
            String detalleSolicitudServicio
    ) {
        public static ResponseCliente fromEntity(OrdenesServicio o) {
            return new ResponseCliente(
                    o.getIdOrdenServicio(),
                    o.getCodigoOrdenServicio(),
                    o.getFechaOrdenServicio(),
                    o.getFechaProgramadaServicio(),
                    o.getSerieComprobanteServicio(),
                    o.getNumeroComprobanteServicio(),
                    o.getFechaEmisionComprobante(),
                    o.getEstadoServicio(),
                    o.getMontoTotalServicio(),
                    o.getMetodoPagoServicio(),
                    o.getIncidenciaServicio(),
                    o.getServicio().getNombreServicio(),
                    o.getVendedor() != null ? o.getVendedor().getNombreUsuario() : "Por asignar",
                    o.getAuxiliar() != null ? o.getAuxiliar().getNombreUsuario() : "Por asignar",
                    o.getDireccionServicio(),
                    o.getDetalleSolicitudServicio()
            );
        }
    }

    public record ResponseVendedor(
            Long idOrdenServicio,
            String codigoOrdenServicio,
            LocalDateTime fechaOrdenServicio,
            LocalDateTime fechaProgramadaServicio,
            String serieComprobanteServicio,
            String numeroComprobanteServicio,
            LocalDateTime fechaEmisionComprobante,
            String estadoServicio,
            double montoTotalServicio,
            String metodoPagoServicio,
            String incidenciaServicio,
            String nombreServicio,

            int cantidadPersonalRequerida,
            int cantidadPersonalAsignada,

            Long idCliente, String nombreCliente, String telefonoCliente,
            Long idVendedor, String nombreVendedor,
            Long idAuxiliar, String nombreAuxiliar,
            String direccionServicio,
            String detalleSolicitudServicio
    ) {
        public static ResponseVendedor fromEntity(OrdenesServicio o) {
            // Manejo seguro por si cantidadPersonalServicio viene como String "3" o "3 personas"
            int requeridos = 0;
            try {
                // Intentamos extraer solo los números del string (ej: "3 personas" -> 3)
                String soloNumeros = o.getServicio().getCantidadPersonalServicio().replaceAll("\\D+", "");
                requeridos = Integer.parseInt(soloNumeros);
            } catch (Exception e) {
                requeridos = 1;
            }

            return new ResponseVendedor(
                    o.getIdOrdenServicio(),
                    o.getCodigoOrdenServicio(),
                    o.getFechaOrdenServicio(),
                    o.getFechaProgramadaServicio(),
                    o.getSerieComprobanteServicio(),
                    o.getNumeroComprobanteServicio(),
                    o.getFechaEmisionComprobante(),
                    o.getEstadoServicio(),
                    o.getMontoTotalServicio(),
                    o.getMetodoPagoServicio(),
                    o.getIncidenciaServicio(),
                    o.getServicio().getNombreServicio(),
                    requeridos,
                    o.getAsignaciones() != null ? o.getAsignaciones().size() : 0,
                    o.getCliente().getIdUsuario(), o.getCliente().getNombreUsuario(), o.getCliente().getTelefonoUsuario(),
                    o.getVendedor() != null ? o.getVendedor().getIdUsuario() : null,
                    o.getVendedor() != null ? o.getVendedor().getNombreUsuario() : null,
                    o.getAuxiliar() != null ? o.getAuxiliar().getIdUsuario() : null,
                    o.getAuxiliar() != null ? o.getAuxiliar().getNombreUsuario() : null,
                    o.getDireccionServicio(),
                    o.getDetalleSolicitudServicio()
            );
        }
    }
}