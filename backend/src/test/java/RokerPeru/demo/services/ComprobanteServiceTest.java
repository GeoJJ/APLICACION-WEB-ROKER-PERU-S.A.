package RokerPeru.demo.services;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.PedidosRepository;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {
    @Mock private PedidosRepository pedidosRepository;
    @Mock private OrdenesServicioRepository ordenesRepository;
    @Mock private EntityManager entityManager;
    @Mock private Query bloqueo;
    @InjectMocks private ComprobanteService comprobanteService;

    private void prepararBloqueo() {
        org.springframework.test.util.ReflectionTestUtils.setField(comprobanteService, "entityManager", entityManager);
        when(entityManager.createNativeQuery("select cast(pg_advisory_xact_lock(:clave) as text)"))
                .thenReturn(bloqueo);
        when(bloqueo.setParameter(eq("clave"), any())).thenReturn(bloqueo);
    }

    @ParameterizedTest @EnumSource(TipoComprobanteEnum.class)
    void emitir_incrementaElUltimoNumeroDeLaSerieYAdquiereBloqueo(TipoComprobanteEnum tipo) {
        prepararBloqueo();
        if (tipo.esServicio()) {
            when(ordenesRepository.ultimoNumeroComprobante(tipo.getSerie())).thenReturn("00000004");
        } else {
            when(pedidosRepository.ultimoNumeroComprobante(tipo.getSerie())).thenReturn("00000004");
        }
        var resultado = comprobanteService.emitir(tipo);
        assertThat(resultado.serie()).isEqualTo(tipo.getSerie());
        assertThat(resultado.numero()).isEqualTo("00000005");
        assertThat(resultado.completo()).isEqualTo(tipo.getSerie() + "-00000005");
        verify(bloqueo).setParameter("clave", (long) ("comprobante:" + tipo.getSerie()).hashCode());
        verify(bloqueo).getSingleResult();
        if (tipo.esServicio()) verifyNoInteractions(pedidosRepository);
        else verifyNoInteractions(ordenesRepository);
    }

    @ParameterizedTest @EnumSource(TipoComprobanteEnum.class)
    void emitir_sinComprobantesPreviosEmpiezaEnUno(TipoComprobanteEnum tipo) {
        prepararBloqueo();
        assertThat(comprobanteService.emitir(tipo).numero()).isEqualTo("00000001");
    }

    @Test void emitir_rellenaConCerosHastaOchoDigitos() {
        prepararBloqueo();
        when(ordenesRepository.ultimoNumeroComprobante("F002")).thenReturn("00012344");
        assertThat(comprobanteService.emitir(TipoComprobanteEnum.FACTURA_SERVICIO).numero())
                .isEqualTo("00012345").hasSize(8);
    }

    @ParameterizedTest @EnumSource(TipoComprobanteEnum.class)
    void resolverTipo_distinguePersonaEmpresaYServicio(TipoComprobanteEnum tipo) {
        Usuario cliente = new Usuario();
        cliente.setTipoPersonaUsuario(tipo.name().startsWith("FACTURA") ? "JURIDICA" : "NATURAL");
        assertThat(ComprobanteService.resolverTipo(cliente, tipo.esServicio())).isEqualTo(tipo);
    }
}
