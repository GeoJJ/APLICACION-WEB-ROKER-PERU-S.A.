package RokerPeru.demo.controller;

import RokerPeru.demo.services.ComprobantePdfService;
import RokerPeru.demo.services.ComprobantePdfService.ComprobantePdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comprobantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
public class ComprobanteController {

    private final ComprobantePdfService comprobantePdfService;

    @GetMapping("/pedidos/{idPedido}")
    public ResponseEntity<byte[]> comprobantePedido(@PathVariable Long idPedido) {
        return responder(comprobantePdfService.generarDePedido(idPedido));
    }

    @GetMapping("/ordenes-servicio/{idOrden}")
    public ResponseEntity<byte[]> comprobanteOrdenServicio(@PathVariable Long idOrden) {
        return responder(comprobantePdfService.generarDeOrdenServicio(idOrden));
    }

    private ResponseEntity<byte[]> responder(ComprobantePdf pdf) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.nombreArchivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf.contenido());
    }
}
