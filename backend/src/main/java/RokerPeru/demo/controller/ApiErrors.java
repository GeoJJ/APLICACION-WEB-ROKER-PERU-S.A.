package RokerPeru.demo.controller;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> status(ResponseStatusException e) { return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Operación rechazada":e.getReason())); }
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(Map.of("message",e.getBindingResult().getAllErrors().get(0).getDefaultMessage())); }
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> constraint(DataIntegrityViolationException e) { return ResponseEntity.status(409).body(Map.of("message","Los datos ya existen o están relacionados con otros registros.")); }
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class}) ResponseEntity<?> invalid(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"Datos no válidos":e.getMessage())); }
 @ExceptionHandler(RuntimeException.class) ResponseEntity<?> business(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"No se pudo completar la operación":e.getMessage())); }
}
