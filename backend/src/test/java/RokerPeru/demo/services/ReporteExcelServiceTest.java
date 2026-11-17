package RokerPeru.demo.services;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteExcelServiceTest {

    private ReporteExcelService reporteExcelService;

    @BeforeEach
    void setUp() {
        reporteExcelService = new ReporteExcelService();
    }

    private Sheet abrirHoja(ByteArrayInputStream stream) throws IOException {
        Workbook workbook = new XSSFWorkbook(stream);
        return workbook.getSheet("Reporte de Productos");
    }

    @Test
    void generarReporteProductosExcel_retornaUnArchivoExcelValido() throws IOException {
        ByteArrayInputStream resultado = reporteExcelService.generarReporteProductosExcel();

        assertThat(resultado).isNotNull();
        assertThat(resultado.available()).isPositive();

        try (Workbook workbook = new XSSFWorkbook(resultado)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
        }
    }

    @Test
    void generarReporteProductosExcel_creaLaHojaConElNombreEsperado() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(reporteExcelService.generarReporteProductosExcel())) {
            assertThat(workbook.getSheetName(0)).isEqualTo("Reporte de Productos");
        }
    }

    @Test
    void generarReporteProductosExcel_incluyeLasColumnasDeLaCabecera() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(reporteExcelService.generarReporteProductosExcel())) {
            Row cabecera = workbook.getSheetAt(0).getRow(0);

            assertThat(cabecera.getLastCellNum()).isEqualTo((short) 5);
            assertThat(cabecera.getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(cabecera.getCell(1).getStringCellValue()).isEqualTo("Nombre");
            assertThat(cabecera.getCell(2).getStringCellValue()).isEqualTo("Categoría");
            assertThat(cabecera.getCell(3).getStringCellValue()).isEqualTo("Precio");
            assertThat(cabecera.getCell(4).getStringCellValue()).isEqualTo("Stock");
        }
    }

    @Test
    void generarReporteProductosExcel_aplicaEstiloALaCabecera() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(reporteExcelService.generarReporteProductosExcel())) {
            Cell celda = workbook.getSheetAt(0).getRow(0).getCell(0);

            assertThat(celda.getCellStyle().getFillPattern()).isEqualTo(FillPatternType.SOLID_FOREGROUND);
            assertThat(workbook.getFontAt(celda.getCellStyle().getFontIndex()).getBold()).isTrue();
        }
    }

    @Test
    void generarReporteProductosExcel_incluyeLaFilaDeDatos() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(reporteExcelService.generarReporteProductosExcel())) {
            Row fila = workbook.getSheetAt(0).getRow(1);

            assertThat(fila.getCell(0).getNumericCellValue()).isEqualTo(1.0);
            assertThat(fila.getCell(1).getStringCellValue()).isEqualTo("Mascarilla KN95");
            assertThat(fila.getCell(2).getStringCellValue()).isEqualTo("Bioseguridad");
            assertThat(fila.getCell(3).getNumericCellValue()).isEqualTo(2.50);
            assertThat(fila.getCell(4).getNumericCellValue()).isEqualTo(100.0);
        }
    }

    @Test
    void generarReporteProductosExcel_cadaLlamadaGeneraUnArchivoIndependiente() {
        ByteArrayInputStream primero = reporteExcelService.generarReporteProductosExcel();
        ByteArrayInputStream segundo = reporteExcelService.generarReporteProductosExcel();

        assertThat(primero).isNotSameAs(segundo);
        assertThat(primero.available()).isEqualTo(segundo.available());
    }
}