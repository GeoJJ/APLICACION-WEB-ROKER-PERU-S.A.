package RokerPeru.demo.exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail handleNoEncontrado(RecursoNoEncontradoException ex) {
        return construirDetalle(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail handleReglaNegocio(ReglaNegocioException ex) {
        return construirDetalle(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler({
            ConflictoException.class,
            OperacionNoPermitidaException.class,
            DataIntegrityViolationException.class
    })
    public ProblemDetail handleConflictosGenerales(RuntimeException ex) {
        if (ex instanceof DataIntegrityViolationException) {
            log.warn("Violación de integridad de datos", ex);
            return construirDetalle(HttpStatus.CONFLICT, "La operación viola una restricción de datos");
        }
        return construirDetalle(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail handleStock(StockInsuficienteException ex) {
        ProblemDetail problema = construirDetalle(HttpStatus.CONFLICT, ex.getMessage());
        problema.setProperty("stockDisponible", ex.getStockDisponible());
        problema.setProperty("cantidadSolicitada", ex.getCantidadSolicitada());
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Error interno no controlado", ex);
        return construirDetalle(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {

        if (body instanceof ProblemDetail problema) {
            problema.setProperty("timestamp", Instant.now());
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail construirDetalle(HttpStatus status, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }
}
