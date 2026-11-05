package RokerPeru.demo.controller;
import RokerPeru.demo.dto.OrdenServicioDTO; import RokerPeru.demo.services.OrdenServicioService; import RokerPeru.demo.security.CurrentUser;
import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/personal/ordenes-servicio") @RequiredArgsConstructor
public class PersonalOrdenServicioController {
 private final OrdenServicioService service; private final CurrentUser current;
 @GetMapping public List<OrdenServicioDTO.ResponseVendedor> listar() { return service.listarParaPersonal(current.get().getIdUsuario()); }
 @GetMapping("/{id}") public OrdenServicioDTO.ResponseVendedor buscar(@PathVariable Long id) { return service.buscarParaPersonal(id,current.get().getIdUsuario()); }
}