package RokerPeru.demo.controller;

import RokerPeru.demo.services.ReporteExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 1. Importar la anotación Slf4j
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@Slf4j // 2. Agregar la anotación aquí
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReporteController {

    private final ReporteExcelService reporteExcelService;

    @GetMapping("/productos/excel")
    public ResponseEntity<Resource> descargarReporteExcel() {
        // 3. Registrar un log de auditoría (Cumple con la trazabilidad solicitada)
        log.info("AUDITORÍA: El Gerente General solicitó la descarga del reporte gerencial de productos en Excel.");

        ByteArrayInputStream actualData = reporteExcelService.generarReporteProductosExcel();
        
        String filename = "reporte_productos.xlsx";
        InputStreamResource file = new InputStreamResource(actualData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}