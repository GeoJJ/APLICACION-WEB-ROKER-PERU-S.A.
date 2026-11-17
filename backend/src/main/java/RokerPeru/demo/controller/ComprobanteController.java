package RokerPeru.demo.controller;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.services.ComprobantePdfService;
import RokerPeru.demo.services.ComprobantePdfService.ComprobantePdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// No se pone produces = PDF a propósito: si algo falla, el GlobalExceptionHandler responde JSON.
@RestController
@RequestMapping("/comprobantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
@PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE_GENERAL','VENDEDOR','CLIENTE')")
public class ComprobanteController {

    private final ComprobantePdfService comprobantePdfService;

    // GET /comprobantes/pedidos/{idPedido}
    @GetMapping("/pedidos/{idPedido}")
    public ResponseEntity<byte[]> comprobantePedido(@PathVariable Long idPedido,
                                                    @AuthenticationPrincipal Usuario usuario) {
        return responder(comprobantePdfService.generarDePedido(idPedido, usuario));
    }

    // GET /comprobantes/ordenes-servicio/{idOrden}
    @GetMapping("/ordenes-servicio/{idOrden}")
    public ResponseEntity<byte[]> comprobanteOrdenServicio(@PathVariable Long idOrden,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return responder(comprobantePdfService.generarDeOrdenServicio(idOrden, usuario));
    }

    private ResponseEntity<byte[]> responder(ComprobantePdf pdf) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.nombreArchivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf.contenido());
    }
}