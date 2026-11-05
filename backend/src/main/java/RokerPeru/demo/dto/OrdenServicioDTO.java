package RokerPeru.demo.dto;

import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.entities.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class OrdenServicioDTO {
    private OrdenServicioDTO() {
    }

    public record RequestCliente(
            @NotNull(message = "El ID del servicio es obligatorio")
            Long idServicio,

            @NotNull(message = "El ID del cliente es obligatorio")
            Long idCliente,

            LocalDateTime fechaProgramadaServicio,

            @NotBlank(message = "La serie del comprobante es obligatoria")
            String serieComprobanteServicio,

            @NotBlank(message = "El número del comprobante es obligatorio")
            String numeroComprobanteServicio,

            @NotBlank(message = "El método de pago es obligatorio")
            String metodoPagoServicio,

            String incidenciaServicio
    ) {
        public RequestCliente {
            serieComprobanteServicio = serieComprobanteServicio != null ? serieComprobanteServicio.trim().toUpperCase() : null;
            numeroComprobanteServicio = numeroComprobanteServicio != null ? numeroComprobanteServicio.trim() : null;
            metodoPagoServicio = metodoPagoServicio != null ? metodoPagoServicio.trim().toUpperCase() : null;
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
            orden.setSerieComprobanteServicio(this.serieComprobanteServicio);
            orden.setNumeroComprobanteServicio(this.numeroComprobanteServicio);
            orden.setMetodoPagoServicio(this.metodoPagoServicio);
            orden.setIncidenciaServicio(this.incidenciaServicio);
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
            LocalDateTime fechaOrdenServicio,
            LocalDateTime fechaProgramadaServicio,
            String serieComprobanteServicio,
            String numeroComprobanteServicio,
            String estadoServicio,
            double montoTotalServicio,
            String metodoPagoServicio,
            String incidenciaServicio,
            String nombreServicio,
            String nombreVendedor,
            String nombreAuxiliar
    ) {
        public static ResponseCliente fromEntity(OrdenesServicio o) {
            return new ResponseCliente(
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
                    o.getVendedor() != null ? o.getVendedor().getNombreUsuario() : "Por asignar",
                    o.getAuxiliar() != null ? o.getAuxiliar().getNombreUsuario() : "Por asignar"
            );
        }
    }

    public record ResponseVendedor(
            Long idOrdenServicio,
            LocalDateTime fechaOrdenServicio,
            LocalDateTime fechaProgramadaServicio,
            String serieComprobanteServicio,
            String numeroComprobanteServicio,
            String estadoServicio,
            double montoTotalServicio,
            String metodoPagoServicio,
            String incidenciaServicio,
            String nombreServicio,

            int cantidadPersonalRequerida,
            int cantidadPersonalAsignada,

            Long idCliente, String nombreCliente,
            Long idVendedor, String nombreVendedor,
            Long idAuxiliar, String nombreAuxiliar
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
                    o.getFechaOrdenServicio(),
                    o.getFechaProgramadaServicio(),
                    o.getSerieComprobanteServicio(),
                    o.getNumeroComprobanteServicio(),
                    o.getEstadoServicio(),
                    o.getMontoTotalServicio(),
                    o.getMetodoPagoServicio(),
                    o.getIncidenciaServicio(),
                    o.getServicio().getNombreServicio(),
                    requeridos,
                    o.getAsignaciones() != null ? o.getAsignaciones().size() : 0,
                    o.getCliente().getIdUsuario(), o.getCliente().getNombreUsuario(),
                    o.getVendedor() != null ? o.getVendedor().getIdUsuario() : null,
                    o.getVendedor() != null ? o.getVendedor().getNombreUsuario() : null,
                    o.getAuxiliar() != null ? o.getAuxiliar().getIdUsuario() : null,
                    o.getAuxiliar() != null ? o.getAuxiliar().getNombreUsuario() : null
            );
        }
    }
}