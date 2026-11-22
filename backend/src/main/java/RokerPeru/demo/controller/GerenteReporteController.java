package RokerPeru.demo.controller;

import RokerPeru.demo.services.ReporteExcelService;
import RokerPeru.demo.services.ReporteExcelService.ReporteExcel;
import RokerPeru.demo.services.ReporteVentasService;
import RokerPeru.demo.services.ReporteVentasService.Alcance;
import RokerPeru.demo.services.ReporteVentasService.ReportePdf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Gerente general: reportes.
 * - Inventario completo en Excel (Apache POI)
 * - Registro de ventas en PDF (JasperReports), por mes o por año, con vista previa
 */
@Slf4j
@RestController
@RequestMapping("/gerente/reportes")
@RequiredArgsConstructor
// exposedHeaders: sin esto el navegador no deja leer el nombre del archivo (Content-Disposition)
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
@PreAuthorize("hasRole('GERENTE_GENERAL')")
public class GerenteReporteController {

    private final ReporteExcelService reporteExcelService;
    private final ReporteVentasService reporteVentasService;

    // GET /gerente/reportes/productos/excel
    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarInventarioExcel() {
        log.info("AUDITORÍA: el gerente general solicitó el registro de inventario en Excel");
        ReporteExcel excel = reporteExcelService.generarReporteInventarioExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + excel.nombreArchivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel.contenido());
    }

    // GET /gerente/reportes/ventas/pdf?anio=2026&mes=7&alcance=TODOS
    //   - con mes  -> ventas de ese mes
    //   - sin mes  -> ventas de todo el año
    // Se responde "inline": el navegador lo muestra en la vista previa (igual que el comprobante).
    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> registroVentasPdf(
            @RequestParam int anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam(defaultValue = "TODOS") Alcance alcance) {
        log.info("AUDITORÍA: el gerente general solicitó el registro de ventas (año={}, mes={}, alcance={})",
                anio, mes, alcance);
        ReportePdf pdf = reporteVentasService.generar(anio, mes, alcance);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pdf.nombreArchivo() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(pdf.contenido());
    }
}