
package RokerPeru.demo.services;

import RokerPeru.demo.logging.RegistroOperacion;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReporteService {

    private static final Logger log =
            LoggerFactory.getLogger(ReporteService.class);

    // Cache para no compilar la misma plantilla varias veces
    private final Map<String, JasperReport> cache =
            new ConcurrentHashMap<>();

    private JasperReport cargar(String nombre) {
        return cache.computeIfAbsent(nombre, key -> {
            String ruta = "reports/" + key + ".jrxml";

            try (InputStream input =
                         new ClassPathResource(ruta).getInputStream()) {

                RegistroOperacion.infoConfirmada(log, "Compilando plantilla Jasper: {}", ruta);

                return JasperCompileManager.compileReport(input);

            } catch (Exception e) {

                throw new IllegalStateException(
                        "No se pudo compilar la plantilla: " + ruta, e
                );
            }
        });
    }

    // Generar documento PDF
    public byte[] generarPdf(
            String plantilla,
            Map<String, Object> parametros,
            Collection<?> filas) {

        try {

            JasperReport reporte = cargar(plantilla);

            JRBeanCollectionDataSource dataSource =
                    new JRBeanCollectionDataSource(filas);

            JasperPrint print =
                    JasperFillManager.fillReport(
                            reporte,
                            parametros,
                            dataSource
                    );

            return JasperExportManager.exportReportToPdf(print);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo generar el reporte: " + plantilla,
                    e
            );
        }
    }
}
