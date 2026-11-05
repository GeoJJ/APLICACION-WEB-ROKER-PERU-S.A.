package RokerPeru.demo.controller;
import RokerPeru.demo.dto.OrdenServicioDTO; import RokerPeru.demo.services.OrdenServicioService; import RokerPeru.demo.security.CurrentUser;
import jakarta.validation.Valid; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/cliente/ordenes-servicio") @RequiredArgsConstructor
public class ClienteOrdenServicioController {
 private final OrdenServicioService service; private final CurrentUser current;
 @GetMapping public List<OrdenServicioDTO.ResponseCliente> listar() { return service.listarOrdenesServicioCliente(current.get().getIdUsuario()); }
 @GetMapping("/{id}") public OrdenServicioDTO.ResponseCliente buscar(@PathVariable Long id) { return service.buscarOrdenServicioCliente(id,current.get().getIdUsuario()); }
 @PostMapping public OrdenServicioDTO.ResponseCliente crear(@Valid @RequestBody OrdenServicioDTO.RequestCliente dto) { return service.crearOrdenServicio(new OrdenServicioDTO.RequestCliente(dto.idServicio(),current.get().getIdUsuario(),dto.fechaProgramadaServicio(),dto.metodoPagoServicio(),dto.incidenciaServicio())); }
 @PutMapping("/{id}/cancelar") public OrdenServicioDTO.ResponseCliente cancelar(@PathVariable Long id) { return service.cancelarCliente(id,current.get().getIdUsuario()); }
}