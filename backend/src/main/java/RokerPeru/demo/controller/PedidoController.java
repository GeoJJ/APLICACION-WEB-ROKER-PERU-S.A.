package RokerPeru.demo.controller;

import RokerPeru.demo.dto.PedidoRequestDTO;
import RokerPeru.demo.dto.PedidoResponseDTO;
import RokerPeru.demo.services.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/crear")
    public PedidoResponseDTO crearPedido(@RequestBody PedidoRequestDTO dto) {
        return pedidoService.crearPedido(dto);
    }

    @GetMapping("/listar")
    public List<PedidoResponseDTO> listarPedido() {
        return pedidoService.listarPedido();
    }

    @GetMapping("/buscar/{id}")
    public PedidoResponseDTO buscarPorIdPedido(@PathVariable Long id) {
        return pedidoService.buscarPorIdPedido(id);
    }

}