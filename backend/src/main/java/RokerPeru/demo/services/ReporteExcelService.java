package RokerPeru.demo.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ReporteExcelService {

    public ByteArrayInputStream generarReporteProductosExcel() {
        // Datos de ejemplo o que puedes conectar luego con tu Repository
        String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Stock"};
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Productos");

            // Crear fuente y estilo para la cabecera
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Fila de cabecera
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Aquí puedes hacer un bucle (loop) con los datos de tu base de datos (ej. List<Producto>)
            // Por ahora dejamos una fila de ejemplo:
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue(1);
            row.createCell(1).setCellValue("Mascarilla KN95");
            row.createCell(2).setCellValue("Bioseguridad");
            row.createCell(3).setCellValue(2.50);
            row.createCell(4).setCellValue(100);

            // Autoajustar tamaño de columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el reporte de Excel: " + e.getMessage());
        }
    }
}