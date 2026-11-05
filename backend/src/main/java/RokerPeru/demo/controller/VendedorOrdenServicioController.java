package RokerPeru.demo.controller;
import RokerPeru.demo.dto.OrdenServicioDTO; import RokerPeru.demo.services.OrdenServicioService;
import jakarta.validation.Valid; import java.util.List; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/vendedor/ordenes-servicio") @RequiredArgsConstructor
public class VendedorOrdenServicioController {
 private final OrdenServicioService service;
 @GetMapping public List<OrdenServicioDTO.ResponseVendedor> listar() { return service.listarOrdenesServicioVendedor(); }
 @GetMapping("/{id}") public OrdenServicioDTO.ResponseVendedor buscar(@PathVariable Long id) { return service.buscarOrdenServicioVendedor(id); }
 @PutMapping("/{id}/confirmar") public OrdenServicioDTO.ResponseVendedor confirmar(@PathVariable Long id,@Valid @RequestBody OrdenServicioDTO.RequestGestionVendedor dto) { return service.confirmarOrdenServicio(id,new OrdenServicioDTO.RequestGestionVendedor("CONFIRMADO",dto.incidenciaServicio())); }
}