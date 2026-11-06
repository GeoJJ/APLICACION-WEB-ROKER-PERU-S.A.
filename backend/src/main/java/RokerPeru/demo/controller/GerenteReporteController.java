package RokerPeru.demo.controller;

import RokerPeru.demo.services.ReporteExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gerente general: descarga de reportes.
 */
@Slf4j
@RestController
@RequestMapping("/gerente/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('GERENTE_GENERAL')")
public class GerenteReporteController {

    private final ReporteExcelService reporteExcelService;

    // GET /gerente/reportes/productos/excel
    @GetMapping("/productos/excel")
    public ResponseEntity<Resource> descargarReporteProductosExcel() {
        log.info("AUDITORÍA: el gerente general solicitó el reporte de productos en Excel");

        Resource archivo = new InputStreamResource(reporteExcelService.generarReporteProductosExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_productos.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }
}
