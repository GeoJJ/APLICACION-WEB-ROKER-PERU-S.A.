package RokerPeru.demo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RegistroOperacionTest {
    private Logger logger;
    private ListAppender<ILoggingEvent> eventos;
    private TransactionTemplate transaccion;

    @BeforeEach void preparar() {
        SecurityContextHolder.clearContext();
        logger = (Logger) LoggerFactory.getLogger("verificacion.registro.operacion");
        logger.setLevel(Level.DEBUG);
        eventos = new ListAppender<>();
        eventos.start();
        logger.addAppender(eventos);
        transaccion = new TransactionTemplate(new AbstractPlatformTransactionManager() {
            @Override protected Object doGetTransaction() { return new Object(); }
            @Override protected void doBegin(Object tx, TransactionDefinition definition) {}
            @Override protected void doCommit(DefaultTransactionStatus status) {}
            @Override protected void doRollback(DefaultTransactionStatus status) {}
        });
    }

    @AfterEach void limpiar() {
        logger.detachAppender(eventos);
        eventos.stop();
        SecurityContextHolder.clearContext();
    }

    @Test void registraElExitoSoloDespuesDeConfirmar() {
        transaccion.executeWithoutResult(status -> {
            RegistroOperacion.infoConfirmada(logger, "Pedido creado: id={}", 12);
            assertThat(eventos.list).isEmpty();
        });
        assertThat(eventos.list).hasSize(1);
        assertThat(eventos.list.getFirst().getFormattedMessage()).contains("Pedido creado: id=12");
    }

    @Test void noAnunciaExitoSiSeRevierte() {
        transaccion.executeWithoutResult(status -> {
            RegistroOperacion.infoConfirmada(logger, "Comprobante emitido: número={}", "00000012");
            status.setRollbackOnly();
        });
        assertThat(eventos.list).isEmpty();
    }

    @Test void conservaElActorAunqueSeLimpieLaSesionAntesDelCommit() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("operador", null, List.of()));
        transaccion.executeWithoutResult(status -> {
            RegistroOperacion.infoConfirmada(logger, "Producto actualizado: id={}", 4);
            SecurityContextHolder.clearContext();
        });
        assertThat(eventos.list.getFirst().getFormattedMessage()).contains("usuario=operador");
    }

    @Test void limpiaSaltosDeLineaYConservaElNivelDeRechazo() {
        RegistroOperacion.rechazo(logger, "Acceso denegado: ruta={}", "/admin\r\nmensaje falso");
        assertThat(eventos.list.getFirst().getLevel()).isEqualTo(Level.WARN);
        assertThat(eventos.list.getFirst().getFormattedMessage()).doesNotContain("\r", "\n");
    }

    @Test void elDebugTampocoSeRegistraSiSeRevierte() {
        transaccion.executeWithoutResult(status -> {
            RegistroOperacion.debugConfirmado(logger, "Código generado: código={}", "SRV-0009");
            status.setRollbackOnly();
        });
        assertThat(eventos.list).isEmpty();
    }
}
