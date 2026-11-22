package RokerPeru.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
        return construirDetalle(org.springframework.http.HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail handleReglaNegocio(ReglaNegocioException ex) {
        return construirDetalle(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler({
            ConflictoException.class,
            OperacionNoPermitidaException.class,
            DataIntegrityViolationException.class
    })
    public ProblemDetail handleConflictosGenerales(RuntimeException ex) {
        if (ex instanceof DataIntegrityViolationException) {
            log.warn("Violación de integridad de datos", ex);
            return construirDetalle(org.springframework.http.HttpStatus.CONFLICT, "La operación viola una restricción de datos");
        }
        return construirDetalle(org.springframework.http.HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail handleStock(StockInsuficienteException ex) {
        ProblemDetail problema = construirDetalle(org.springframework.http.HttpStatus.CONFLICT, ex.getMessage());
        problema.setProperty("stockDisponible", ex.getStockDisponible());
        problema.setProperty("cantidadSolicitada", ex.getCantidadSolicitada());
        return problema;
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ProblemDetail handleSolicitudInvalida(SolicitudInvalidaException ex) {
        return construirDetalle(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Login con credenciales malas
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAutenticacion(AuthenticationException ex) {
        return construirDetalle(org.springframework.http.HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }

    // Rol sin permiso (@PreAuthorize)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccesoDenegado(AccessDeniedException ex) {
        return construirDetalle(org.springframework.http.HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta operación");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        log.error("Error interno no controlado", ex);
        return construirDetalle(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
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

    private ProblemDetail construirDetalle(org.springframework.http.HttpStatus status, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalle);
        problema.setProperty("timestamp", Instant.now());
        return problema;
    }
}