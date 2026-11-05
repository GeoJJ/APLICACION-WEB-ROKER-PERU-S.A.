package RokerPeru.demo.dto;

import RokerPeru.demo.entities.MovimientosProducto;
import RokerPeru.demo.entities.Producto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public final class MovimientoDTO {
    private MovimientoDTO() {
    }

    // Entrada para registrarIngreso / registrarSalida / registrarAjuste
    public record Request(
            @NotNull(message = "El producto es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer cantidad,

            // Informativo: el tipo real lo define el método (ingreso / salida / ajuste)
            String tipoMovimiento,

            String justificacion
    ) {
    }

    // Respuesta de una operación y del historial
    public record Response(
            Long idMovimiento,
            String nombreProducto,
            String tipoMovimiento,
            int cantidadMovida,
            int stockResultante,
            LocalDateTime fechaOperacion,
            String mensaje
    ) {
        public static Response fromEntity(MovimientosProducto m, int stockResultante, String mensaje) {
            return new Response(
                    m.getIdMovimientoProducto(),
                    m.getProducto().getNombreProducto(),
                    m.getTipoMovimientoProducto(),
                    m.getCantidadMovimientoProducto(),
                    stockResultante,
                    m.getFechaMovimientoProducto(),
                    mensaje
            );
        }
    }

    // Alertas de stock bajo (Almacenista / Admin)
    public record AlertaStockResponse(
            Long productoId,
            String nombreProducto,
            Integer stockActual,
            Integer stockMinimo,
            String nivelUrgencia
    ) {
        public static AlertaStockResponse fromEntity(Producto p) {
            return new AlertaStockResponse(
                    p.getIdProducto(),
                    p.getNombreProducto(),
                    p.getStockProducto(),
                    p.getStockMinimo(),
                    calcularNivelUrgencia(p.getStockProducto(), p.getStockMinimo())
            );
        }
    }

    // Cálculo automático del nivel de urgencia
    private static String calcularNivelUrgencia(int stockActual, int stockMinimo) {
        if (stockActual == 0) {
            return "AGOTADO - Reponer inmediatamente";
        } else if (stockActual <= (stockMinimo / 2)) {
            return "CRÍTICO - Por debajo del 50% del mínimo";
        } else if (stockActual <= (stockMinimo + (stockMinimo / 2))) {
            return "ADVERTENCIA - Acercándose al límite";
        }
        return "NINGUNA - Stock aceptable";
    }
}