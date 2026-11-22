package RokerPeru.demo.services;

import RokerPeru.demo.enums.TipoCodigoEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

//Genera los códigos cortos (PRD-0001, PED-0001, SRV-0001, ORD-0001)

@Service
public class CodigoService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public String generar(TipoCodigoEnum tipo) {
        String prefijo = tipo.getPrefijo();

        entityManager.createNativeQuery("select cast(pg_advisory_xact_lock(:clave) as text)")
                .setParameter("clave", (long) ("codigo:" + prefijo).hashCode())
                .getSingleResult();

        // Guarda primero los cambios pendientes de esta transacción, así el máximo ya incluye los códigos recién asignados
        entityManager.flush();

        // Solo cuentan los códigos con el formato PRE-0000 (un producto antiguo con código manual como "MASC-001" se ignora).
        // El número se compara como número, no como texto, para que PED-10000 sea mayor que PED-9999.
        String sql = "select coalesce(max(cast(substring(" + tipo.getColumna() + " from " + (prefijo.length() + 2) + ") as bigint)), 0) "
                + "from " + tipo.getTabla() + " where " + tipo.getColumna() + " ~ '^" + prefijo + "-[0-9]+$'";
        Number ultimo = (Number) entityManager.createNativeQuery(sql).getSingleResult();

        return String.format("%s-%04d", prefijo, ultimo.longValue() + 1);
    }
}