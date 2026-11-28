package RokerPeru.demo.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // Herramienta de Spring que se encarga de enviar el correo
    private final JavaMailSender mailSender;

    // Correo desde el que se envía (el MAIL_USERNAME del .env)
    @Value("${spring.mail.username}")
    private String remitente;

    // Nombre de la empresa, tomado del application.properties
    @Value("${empresa.razon-social}")
    private String razonSocial;

    public void enviarComprobante(String correo, String nombre, String operacion,
                                  String numeroComprobante,
                                  ComprobantePdfService.ComprobantePdf pdf) {

        // Si el cliente no tiene correo registrado, no se envía nada
        if (correo == null || correo.isBlank()) {
            log.warn("No se envió el comprobante {}: el cliente no tiene correo", numeroComprobante);
            return;
        }

        try {
            // Se crea el correo vacío y su ayudante, el true permite adjuntar archivos
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            // Si el comprobante empieza con F es factura, si no es boleta
            String tipoDoc = numeroComprobante.toUpperCase().startsWith("F") ? "factura" : "boleta";

            // Quién envía, a quién le llega y el asunto
            helper.setFrom(remitente);
            helper.setTo(correo);
            helper.setSubject(razonSocial + " - Su " + operacion + " ha sido aprobado");

            // Cuerpo del mensaje en texto plano
            helper.setText("""
                    Estimado(a) %s:

                    Le informamos que su %s ha sido aprobado satisfactoriamente.

                    Adjuntamos a este correo su %s electrónica N° %s en formato PDF.

                    Gracias por su preferencia.

                    Atentamente,
                    %s

                    Este es un mensaje automático, por favor no responda a este correo.
                    """.formatted(nombre, operacion, tipoDoc, numeroComprobante, razonSocial));

            // Se adjunta el PDF de la boleta o factura
            helper.addAttachment(pdf.nombreArchivo(), new ByteArrayResource(pdf.contenido()));

            // Se envía el correo
            mailSender.send(mensaje);
            log.info("Comprobante {} enviado a {}", numeroComprobante, correo);

        } catch (Exception e) {
            // Si algo falla solo se registra el error, la aprobación ya quedó guardada
            log.error("No se pudo enviar el comprobante {} a {}", numeroComprobante, correo, e);
        }
    }
}