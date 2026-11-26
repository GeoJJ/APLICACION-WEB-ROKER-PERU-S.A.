package RokerPeru.demo.services;

import RokerPeru.demo.logging.RegistroOperacion;
import lombok.extern.slf4j.Slf4j;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.PedidosRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final PedidosRepository pedidosRepository;
    private final OrdenesServicioRepository ordenesRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Guarda serie y número por separado: "B001" y "00000001"
    public record NumeroComprobante(String serie, String numero) {
        public String completo() {
            return serie + "-" + numero;
        }
    }

    // Empresa (JURIDICA) = Factura, persona (NATURAL) = Boleta
    public static TipoComprobanteEnum resolverTipo(Usuario cliente, boolean esServicio) {
        boolean esEmpresa = "JURIDICA".equalsIgnoreCase(cliente.getTipoPersonaUsuario());
        if (esServicio) {
            return esEmpresa ? TipoComprobanteEnum.FACTURA_SERVICIO : TipoComprobanteEnum.BOLETA_SERVICIO;
        }
        return esEmpresa ? TipoComprobanteEnum.FACTURA_PRODUCTO : TipoComprobanteEnum.BOLETA_PRODUCTO;
    }

    /**
     * Siguiente número de la serie, SIN tabla de secuencias: se toma el mayor número ya emitido en pedidos
     * (B001/F001) o en órdenes de servicio (B002/F002) y se le suma 1.
     *
     * Para que dos ventas simultáneas no tomen el mismo número se usa un bloqueo de PostgreSQL que dura hasta
     * que termina la transacción (commit o rollback): mientras alguien toma el número de una serie, los demás
     * esperan su turno. Si la transacción falla, se libera sin dejar huecos. Además, las tablas pedidos y
     * ordenes_servicio tienen una restricción UNIQUE (serie, número) como segunda barrera.
     *
     * Debe llamarse dentro de la transacción que guarda el comprobante (así lo hacen PedidoService y OrdenServicioService).
     */
    @Transactional
    public NumeroComprobante emitir(TipoComprobanteEnum tipo) {
        String serie = tipo.getSerie();

        entityManager.createNativeQuery("select cast(pg_advisory_xact_lock(:clave) as text)")
                .setParameter("clave", (long) ("comprobante:" + serie).hashCode())
                .getSingleResult();

        String ultimo = tipo.esServicio()
                ? ordenesRepository.ultimoNumeroComprobante(serie)
                : pedidosRepository.ultimoNumeroComprobante(serie);

        int siguiente = (ultimo == null ? 0 : Integer.parseInt(ultimo)) + 1;
        NumeroComprobante numero = new NumeroComprobante(serie, String.format("%08d", siguiente));
        RegistroOperacion.infoConfirmada(log, "Comprobante emitido: serie={}, número={}", numero.serie(), numero.numero());
        return numero;
    }
}