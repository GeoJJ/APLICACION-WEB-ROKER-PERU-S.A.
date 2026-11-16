
package RokerPeru.demo.controller;

import RokerPeru.demo.dto.ComprobanteDTO;
import RokerPeru.demo.services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/admin/reportes-prueba")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ReportePruebaController {

    private final ReporteService reporteService;

    @GetMapping(value = "/comprobante",
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> comprobante() {

        Map<String, Object> p = new HashMap<>();

        // EMPRESA
        p.put("razonSocial", "ROKER PERU S.A.");
        p.put("rucEmpresa", "20000000001");
        p.put("direccionEmpresa", "Av. Ejemplo 123, Arequipa");

        // COMPROBANTE
        p.put("tipoComprobante", "BOLETA DE VENTA");
        p.put("tipoOperacion", "Venta interna");
        p.put("numeroComprobante", "B001-00000001");
        p.put("fechaEmision", new Date());
        p.put("formaPago", "Contado");

        // CLIENTE
        p.put("clienteNombre", "Cliente de Prueba");
        p.put("clienteDocumentoTipo", "DNI");
        p.put("clienteDocumentoNumero", "12345678");

        // TOTALES
        p.put("subtotal", new BigDecimal("100.00"));
        p.put("impuestoMonto", new BigDecimal("18.00"));
        p.put("total", new BigDecimal("118.00"));

        // DETALLE
        List<ComprobanteDTO> filas = List.of(
                new ComprobanteDTO(
                        "Producto de prueba",
                        new BigDecimal("2"),
                        new BigDecimal("50.00"),
                        new BigDecimal("100.00")
                )
        );

        byte[] pdf = reporteService.generarPdf(
                "comprobante", p, filas
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=comprobante.pdf"
                )
                .body(pdf);
    }
}
