package RokerPeru.demo.services;

import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReporteExcelServiceTest {
    private ProductoRepository repositorio;
    private ReporteExcelService servicio;

    @BeforeEach void setUp() {
        repositorio = mock(ProductoRepository.class);
        servicio = new ReporteExcelService(repositorio);
        ReflectionTestUtils.setField(servicio, "razonSocial", "ROKER PERU S.A.");
        when(repositorio.findAllParaInventario()).thenReturn(List.of());
    }
    private Workbook abrir() throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(servicio.generarReporteInventarioExcel().contenido()));
    }
    private Producto producto(int stock, int minimo) {
        Producto p = new Producto();
        p.setCodigoProducto("PRD-0001"); p.setNombreProducto("Mascarilla KN95");
        p.setCategoriaProducto("BIOSEGURIDAD"); p.setPrecioProducto(2.50);
        p.setStockProducto(stock); p.setStockMinimo(minimo); p.setEstadoProducto("ACTIVO");
        return p;
    }
    @Test void generarReporte_retornaExcelValidoConNombreDescargable() throws IOException {
        var reporte = servicio.generarReporteInventarioExcel();
        assertThat(reporte.nombreArchivo()).endsWith(".xlsx");
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(reporte.contenido()))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(1);
            assertThat(wb.getSheetName(0)).isEqualTo("Inventario");
        }
    }
    @Test void generarReporte_incluyeEmpresaYColumnasDeInventario() throws IOException {
        try (Workbook wb = abrir()) {
            var hoja = wb.getSheetAt(0);
            assertThat(hoja.getRow(1).getCell(0).getStringCellValue()).contains("ROKER PERU S.A.");
            assertThat(hoja.getRow(3).getLastCellNum()).isEqualTo((short)11);
            assertThat(hoja.getRow(3).getCell(2).getStringCellValue()).isEqualTo("Producto");
        }
    }
    @Test void generarReporte_utilizaLosDatosRealesYCalculaElValor() throws IOException {
        when(repositorio.findAllParaInventario()).thenReturn(List.of(producto(100, 10)));
        try (Workbook wb = abrir()) {
            var fila = wb.getSheetAt(0).getRow(4);
            assertThat(fila.getCell(1).getStringCellValue()).isEqualTo("PRD-0001");
            assertThat(fila.getCell(2).getStringCellValue()).isEqualTo("Mascarilla KN95");
            assertThat(fila.getCell(9).getNumericCellValue()).isEqualTo(250.0);
            assertThat(fila.getCell(8).getStringCellValue()).isEqualTo("NORMAL");
        }
    }
    @Test void generarReporte_distingueAgotadoYStockBajo() throws IOException {
        when(repositorio.findAllParaInventario()).thenReturn(List.of(producto(0,10), producto(5,10)));
        try (Workbook wb = abrir()) {
            assertThat(wb.getSheetAt(0).getRow(4).getCell(8).getStringCellValue()).isEqualTo("AGOTADO");
            assertThat(wb.getSheetAt(0).getRow(5).getCell(8).getStringCellValue()).isEqualTo("STOCK BAJO");
        }
    }
    @Test void generarReporte_incluyeTotalesFiltrables() throws IOException {
        when(repositorio.findAllParaInventario()).thenReturn(List.of(producto(100,10)));
        try (Workbook wb = abrir()) {
            assertThat(wb.getSheetAt(0).getRow(5).getCell(6).getCellFormula()).isEqualTo("SUBTOTAL(109,G5:G5)");
            assertThat(wb.getSheetAt(0).getRow(5).getCell(9).getCellFormula()).isEqualTo("SUBTOTAL(109,J5:J5)");
        }
    }
    @Test void generarReporte_sinProductosNoInventaDatos() throws IOException {
        try (Workbook wb = abrir()) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(4);
            assertThat(wb.getSheetAt(0).getRow(4).getCell(5).getStringCellValue()).isEqualTo("TOTAL");
        }
    }
}
