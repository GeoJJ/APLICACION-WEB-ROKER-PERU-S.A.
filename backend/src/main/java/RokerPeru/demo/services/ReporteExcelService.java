package RokerPeru.demo.services;

import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Registro de inventario en Excel: TODOS los productos con su stock real (datos de la BD).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteExcelService {

    private static final String[] COLUMNAS = {"N°", "Código", "Producto", "Categoría", "Proveedor",
            "Precio (S/)", "Stock", "Stock mínimo", "Situación", "Valor en stock (S/)", "Estado"};
    // Ancho de cada columna en caracteres (fijo: autoSizeColumn falla en servidores sin fuentes gráficas)
    private static final int[] ANCHOS = {6, 12, 40, 22, 28, 14, 10, 14, 14, 20, 12};

    private static final int FILA_CABECERA = 3; // 0 título, 1 empresa/fecha, 2 vacía, 3 cabecera

    private final ProductoRepository productoRepository;

    @Value("${empresa.razon-social}")
    private String razonSocial;

    public record ReporteExcel(String nombreArchivo, byte[] contenido) {
    }

    @Transactional(readOnly = true)
    public ReporteExcel generarReporteInventarioExcel() {
        List<Producto> productos = productoRepository.findAllParaInventario();
        LocalDateTime ahora = LocalDateTime.now();

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Inventario");
            Estilos st = new Estilos(wb);

            // Título y datos del reporte
            Row titulo = hoja.createRow(0);
            titulo.setHeightInPoints(26);
            celda(titulo, 0, "REGISTRO DE INVENTARIO", st.titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS.length - 1));

            Row sub = hoja.createRow(1);
            celda(sub, 0, razonSocial + "  |  Generado el "
                    + ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + "  |  " + productos.size() + " productos", st.subtitulo);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, COLUMNAS.length - 1));

            // Cabecera
            Row cab = hoja.createRow(FILA_CABECERA);
            cab.setHeightInPoints(22);
            for (int i = 0; i < COLUMNAS.length; i++) {
                celda(cab, i, COLUMNAS[i], st.cabecera);
                hoja.setColumnWidth(i, ANCHOS[i] * 256);
            }

            // Datos
            int fila = FILA_CABECERA + 1;
            int n = 1;
            for (Producto p : productos) {
                Row r = hoja.createRow(fila++);
                int stock = p.getStockProducto();
                String situacion = stock <= 0 ? "AGOTADO" : stock <= p.getStockMinimo() ? "STOCK BAJO" : "NORMAL";

                numero(r, 0, n++, st.entero);
                celda(r, 1, vacio(p.getCodigoProducto()), st.texto);
                celda(r, 2, vacio(p.getNombreProducto()), st.texto);
                celda(r, 3, vacio(p.getCategoriaProducto()), st.texto);
                celda(r, 4, p.getProveedor() != null ? vacio(p.getProveedor().getNombreProveedor()) : "-", st.texto);
                numero(r, 5, p.getPrecioProducto(), st.dinero);
                numero(r, 6, stock, st.entero);
                numero(r, 7, p.getStockMinimo(), st.entero);
                celda(r, 8, situacion, switch (situacion) {
                    case "AGOTADO" -> st.agotado;
                    case "STOCK BAJO" -> st.bajo;
                    default -> st.normal;
                });
                numero(r, 9, p.getPrecioProducto() * stock, st.dinero);
                celda(r, 10, vacio(p.getEstadoProducto()), st.centro);
            }

            int ultimaDatos = fila - 1;
            // Totales con SUBTOTAL: respetan los filtros que aplique el gerente en Excel
            Row total = hoja.createRow(fila);
            for (int i = 0; i < COLUMNAS.length; i++) celda(total, i, "", st.total);
            celda(total, 5, "TOTAL", st.total);
            if (!productos.isEmpty()) {
                formula(total, 6, "SUBTOTAL(109,G" + (FILA_CABECERA + 2) + ":G" + (ultimaDatos + 1) + ")", st.totalEntero);
                formula(total, 9, "SUBTOTAL(109,J" + (FILA_CABECERA + 2) + ":J" + (ultimaDatos + 1) + ")", st.totalDinero);
                hoja.setAutoFilter(new CellRangeAddress(FILA_CABECERA, ultimaDatos, 0, COLUMNAS.length - 1));
            }
            hoja.createFreezePane(0, FILA_CABECERA + 1); // la cabecera queda fija al desplazarse

            // Calcula las fórmulas ya: así también se ven bien en visores que no recalculan.
            // Si algo fallara, Excel las recalcula solo al abrir el archivo.
            try {
                wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
            } catch (RuntimeException e) {
                log.warn("No se pudieron precalcular las fórmulas del inventario: {}", e.getMessage());
            }
            wb.setForceFormulaRecalculation(true);

            configurarImpresion(hoja);
            wb.write(out);
            String nombre = "inventario-" + ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
            log.info("AUDITORÍA: registro de inventario generado ({} productos)", productos.size());
            return new ReporteExcel(nombre, out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Error al generar el reporte de inventario: " + e.getMessage(), e);
        }
    }

    // Utilidades

    // Hoja horizontal, ajustada al ancho de una página al imprimir
    private static void configurarImpresion(Sheet hoja) {
        hoja.getPrintSetup().setLandscape(true);
        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 0);
    }

    private static String vacio(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private static void celda(Row r, int col, String valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private static void numero(Row r, int col, double valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    private static void formula(Row r, int col, String f, CellStyle estilo) {
        Cell c = r.getCell(col) != null ? r.getCell(col) : r.createCell(col);
        c.setCellFormula(f);
        c.setCellStyle(estilo);
    }

    // Estilos del libro (se crean una sola vez: Excel limita la cantidad de estilos)
    private static final class Estilos {
        final CellStyle titulo, subtitulo, cabecera, texto, centro, entero, dinero, normal, bajo, agotado,
                total, totalEntero, totalDinero;

        Estilos(XSSFWorkbook wb) {
            XSSFColor azul = new XSSFColor(new byte[]{0, 0x42, (byte) 0x8A}, null); // #00428A, igual que el comprobante
            DataFormat fmt = wb.createDataFormat();
            short moneda = fmt.getFormat("\"S/ \"#,##0.00");
            short enteroFmt = fmt.getFormat("#,##0");

            XSSFFont fTitulo = fuente(wb, 16, true, azul);
            XSSFFont fCab = fuente(wb, 10, true, new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            XSSFFont fNegrita = fuente(wb, 10, true, null);

            titulo = base(wb, fTitulo, null, HorizontalAlignment.LEFT, false, (short) 0);
            subtitulo = base(wb, fuente(wb, 9, false, null), null, HorizontalAlignment.LEFT, false, (short) 0);
            cabecera = base(wb, fCab, azul, HorizontalAlignment.CENTER, true, (short) 0);
            texto = base(wb, null, null, HorizontalAlignment.LEFT, true, (short) 0);
            centro = base(wb, null, null, HorizontalAlignment.CENTER, true, (short) 0);
            entero = base(wb, null, null, HorizontalAlignment.RIGHT, true, enteroFmt);
            dinero = base(wb, null, null, HorizontalAlignment.RIGHT, true, moneda);
            normal = base(wb, fNegrita, color(198, 239, 206), HorizontalAlignment.CENTER, true, (short) 0);
            bajo = base(wb, fNegrita, color(255, 235, 156), HorizontalAlignment.CENTER, true, (short) 0);
            agotado = base(wb, fNegrita, color(255, 199, 206), HorizontalAlignment.CENTER, true, (short) 0);
            total = base(wb, fNegrita, color(221, 235, 247), HorizontalAlignment.RIGHT, true, (short) 0);
            totalEntero = base(wb, fNegrita, color(221, 235, 247), HorizontalAlignment.RIGHT, true, enteroFmt);
            totalDinero = base(wb, fNegrita, color(221, 235, 247), HorizontalAlignment.RIGHT, true, moneda);
        }

        private static XSSFColor color(int r, int g, int b) {
            return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, null);
        }

        private static XSSFFont fuente(XSSFWorkbook wb, int pt, boolean negrita, XSSFColor color) {
            XSSFFont f = wb.createFont();
            f.setFontHeightInPoints((short) pt);
            f.setBold(negrita);
            if (color != null) f.setColor(color);
            return f;
        }

        private static CellStyle base(XSSFWorkbook wb, XSSFFont fuente, XSSFColor fondo,
                                      HorizontalAlignment h, boolean borde, short formato) {
            XSSFCellStyle s = wb.createCellStyle();
            if (fuente != null) s.setFont(fuente);
            if (fondo != null) {
                s.setFillForegroundColor(fondo);
                s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            s.setAlignment(h);
            s.setVerticalAlignment(VerticalAlignment.CENTER);
            if (formato != 0) s.setDataFormat(formato);
            if (borde) {
                s.setBorderBottom(BorderStyle.THIN);
                s.setBorderTop(BorderStyle.THIN);
                s.setBorderLeft(BorderStyle.THIN);
                s.setBorderRight(BorderStyle.THIN);
            }
            return s;
        }
    }
}