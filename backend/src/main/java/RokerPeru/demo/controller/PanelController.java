package RokerPeru.demo.controller;
import RokerPeru.demo.services.PanelService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/panel") @RequiredArgsConstructor
public class PanelController {
 private final PanelService service;
 @GetMapping public Map<String,Object> snapshot(){ return service.snapshot(); }
 @PostMapping("/{kind}") public Map<String,Object> create(@PathVariable String kind,@RequestBody Map<String,Object> body){service.save(kind,null,body);return service.snapshot();}
 @PutMapping("/{kind}/{id}") public Map<String,Object> update(@PathVariable String kind,@PathVariable String id,@RequestBody Map<String,Object> body){service.save(kind,id,body);return service.snapshot();}
}
