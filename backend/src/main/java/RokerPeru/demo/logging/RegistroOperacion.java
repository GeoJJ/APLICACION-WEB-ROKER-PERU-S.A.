package RokerPeru.demo.logging;

import RokerPeru.demo.entities.Usuario;
import org.slf4j.Logger;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.Arrays;
import java.util.stream.Stream;

public final class RegistroOperacion {
    private RegistroOperacion() {}

    public static void infoConfirmada(Logger logger, String mensaje, Object... argumentos) {
        Object[] datos = datos(argumentos);
        alConfirmar(() -> logger.info("usuario={} | " + mensaje, datos));
    }

    public static void debugConfirmado(Logger logger, String mensaje, Object... argumentos) {
        if (!logger.isDebugEnabled()) return;
        Object[] datos = datos(argumentos);
        alConfirmar(() -> logger.debug("usuario={} | " + mensaje, datos));
    }

    public static void rechazo(Logger logger, String mensaje, Object... argumentos) {
        logger.warn("usuario={} | " + mensaje, datos(argumentos));
    }

    private static Object[] datos(Object[] argumentos) {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        Object actor = "anonimo";
        if (autenticacion != null && autenticacion.isAuthenticated()
                && !(autenticacion instanceof AnonymousAuthenticationToken)) {
            actor = autenticacion.getPrincipal() instanceof Usuario usuario
                    ? usuario.getIdUsuario() : autenticacion.getName();
        }
        return Stream.concat(Stream.of(actor), Arrays.stream(argumentos))
                .map(valor -> valor instanceof String texto ? texto.replace('\r', ' ').replace('\n', ' ') : valor)
                .toArray();
    }

    private static void alConfirmar(Runnable registro) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { registro.run(); }
            });
        } else {
            registro.run();
        }
    }
}
