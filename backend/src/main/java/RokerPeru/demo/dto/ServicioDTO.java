package RokerPeru.demo.dto;

import RokerPeru.demo.entities.Servicio;
import jakarta.validation.constraints.*;

public final class ServicioDTO {
    private ServicioDTO() {
    }

    public record Request(
            @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
            String categoriaServicio,

            @NotBlank(message = "El nombre del servicio es obligatorio")
            @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
            String nombreServicio,

            @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
            String tipoServicio,

            String descripcionServicio,

            @NotNull(message = "El precio es obligatorio")
            @PositiveOrZero(message = "El precio no puede ser negativo")
            Double precioServicio,

            @Size(max = 50, message = "La duración no puede superar los 50 caracteres")
            String duracionEstimadaServicio,

            @Size(max = 50, message = "La cantidad de personal no puede superar los 50 caracteres")
            String cantidadPersonalServicio
    ) {
        public Request {
            categoriaServicio = categoriaServicio != null ? categoriaServicio.trim().toUpperCase() : null;
            nombreServicio = nombreServicio != null ? nombreServicio.trim() : null;
            tipoServicio = tipoServicio != null ? tipoServicio.trim().toUpperCase() : null;
            descripcionServicio = descripcionServicio != null ? descripcionServicio.trim() : null;
            duracionEstimadaServicio = duracionEstimadaServicio != null ? duracionEstimadaServicio.trim() : null;
            cantidadPersonalServicio = cantidadPersonalServicio != null ? cantidadPersonalServicio.trim() : null;
        }

        public Servicio toEntity() {
            Servicio servicio = new Servicio();
            applyTo(servicio);
            return servicio;
        }

        public void applyTo(Servicio servicio) {
            servicio.setCategoriaServicio(this.categoriaServicio);
            servicio.setNombreServicio(this.nombreServicio);
            servicio.setTipoServicio(this.tipoServicio);
            servicio.setDescripcionServicio(this.descripcionServicio);
            servicio.setPrecioServicio(this.precioServicio);
            servicio.setDuracionEstimadaServicio(this.duracionEstimadaServicio);
            servicio.setCantidadPersonalServicio(this.cantidadPersonalServicio);
        }
    }

    // Vista Admin / Vendedor
    public record Response(
            Long idServicio,
            String categoriaServicio,
            String nombreServicio,
            String tipoServicio,
            String descripcionServicio,
            Double precioServicio,
            String duracionEstimadaServicio,
            String cantidadPersonalServicio
    ) {
        public static Response fromEntity(Servicio s) {
            return new Response(
                    s.getIdServicio(),
                    s.getCategoriaServicio(),
                    s.getNombreServicio(),
                    s.getTipoServicio(),
                    s.getDescripcionServicio(),
                    s.getPrecioServicio(),
                    s.getDuracionEstimadaServicio(),
                    s.getCantidadPersonalServicio()
            );
        }
    }

    // Vista Cliente
    public record ClienteResponse(
            String categoriaServicio,
            String nombreServicio,
            String tipoServicio,
            String descripcionServicio,
            Double precioServicio,
            String duracionEstimadaServicio,
            String cantidadPersonalServicio
    ) {
        public static ClienteResponse fromEntity(Servicio s) {
            return new ClienteResponse(
                    s.getCategoriaServicio(),
                    s.getNombreServicio(),
                    s.getTipoServicio(),
                    s.getDescripcionServicio(),
                    s.getPrecioServicio(),
                    s.getDuracionEstimadaServicio(),
                    s.getCantidadPersonalServicio()
            );
        }
    }
}