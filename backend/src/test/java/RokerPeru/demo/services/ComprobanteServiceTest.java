package RokerPeru.demo.services;

import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.PedidosRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {

    @Mock
    private PedidosRepository pedidosRepository;

    @Mock
    private OrdenesServicioRepository ordenesRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query queryBloqueo;

    private ComprobanteService comprobanteService;

    @BeforeEach
    void setUp() {
        // El servicio recibe los repositorios por constructor, pero el EntityManager entra por
        // @PersistenceContext (campo). Mockito no mezcla ambos tipos de inyección, así que se asigna a mano.
        comprobanteService = new ComprobanteService(pedidosRepository, ordenesRepository);
        ReflectionTestUtils.setField(comprobanteService, "entityManager", entityManager);
    }

    // Prepara el bloqueo de PostgreSQL (select cast(pg_advisory_xact_lock(:clave) as text))
    private void stubBloqueo() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryBloqueo);
        when(queryBloqueo.setParameter(anyString(), any())).thenReturn(queryBloqueo);
        when(queryBloqueo.getSingleResult()).thenReturn("");
    }

    private Usuario clienteConTipoPersona(String tipoPersona) {
        Usuario cliente = new Usuario();
        cliente.setTipoPersonaUsuario(tipoPersona);
        return cliente;
    }

    // ---------- resolverTipo ----------

    @ParameterizedTest
    @CsvSource({
            "JURIDICA,false,FACTURA_PRODUCTO",
            "JURIDICA,true,FACTURA_SERVICIO",
            "NATURAL,false,BOLETA_PRODUCTO",
            "NATURAL,true,BOLETA_SERVICIO"
    })
    void resolverTipo_segunTipoDePersonaYOperacion_retornaElComprobanteCorrecto(
            String tipoPersona, boolean esServicio, TipoComprobanteEnum esperado) {
        Usuario cliente = clienteConTipoPersona(tipoPersona);

        assertThat(ComprobanteService.resolverTipo(cliente, esServicio)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"juridica", "Juridica", "JuRiDiCa"})
    void resolverTipo_ignoraMayusculasYMinusculasEnTipoJuridica(String tipoPersona) {
        Usuario cliente = clienteConTipoPersona(tipoPersona);

        assertThat(ComprobanteService.resolverTipo(cliente, false)).isEqualTo(TipoComprobanteEnum.FACTURA_PRODUCTO);
        assertThat(ComprobanteService.resolverTipo(cliente, true)).isEqualTo(TipoComprobanteEnum.FACTURA_SERVICIO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "OTRO", "EXTRANJERA"})
    void resolverTipo_conTipoDePersonaDesconocidoONulo_emiteBoleta(String tipoPersona) {
        Usuario cliente = clienteConTipoPersona(tipoPersona);

        assertThat(ComprobanteService.resolverTipo(cliente, false)).isEqualTo(TipoComprobanteEnum.BOLETA_PRODUCTO);
        assertThat(ComprobanteService.resolverTipo(cliente, true)).isEqualTo(TipoComprobanteEnum.BOLETA_SERVICIO);
    }

    // ---------- emitir: numeración ----------

    @Test
    void emitir_cuandoNoHayComprobantesPrevios_empiezaEnElNumeroUno() {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("B001")).thenReturn(null);

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);

        assertThat(resultado.serie()).isEqualTo("B001");
        assertThat(resultado.numero()).isEqualTo("00000001");
        assertThat(resultado.completo()).isEqualTo("B001-00000001");
    }

    @ParameterizedTest
    @CsvSource({
            "00000001,00000002",
            "00000009,00000010",
            "00000099,00000100",
            "00012344,00012345",
            "99999998,99999999"
    })
    void emitir_conUltimoNumeroExistente_retornaElSiguienteRellenadoConCeros(String ultimo, String esperado) {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("F001")).thenReturn(ultimo);

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(TipoComprobanteEnum.FACTURA_PRODUCTO);

        assertThat(resultado.numero()).isEqualTo(esperado).hasSize(8);
        assertThat(resultado.completo()).isEqualTo("F001-" + esperado);
    }

    @Test
    void emitir_alSuperarLosOchoDigitos_noTruncaElNumero() {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("B001")).thenReturn("99999999");

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);

        assertThat(resultado.numero()).isEqualTo("100000000");
    }

    @Test
    void emitir_conUltimoNumeroNoNumerico_lanzaNumberFormatException() {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("B001")).thenReturn("ABC");

        assertThatThrownBy(() -> comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO))
                .isInstanceOf(NumberFormatException.class);
    }

    // ---------- emitir: de dónde se toma el último número ----------

    @ParameterizedTest
    @EnumSource(value = TipoComprobanteEnum.class, names = {"BOLETA_PRODUCTO", "FACTURA_PRODUCTO"})
    void emitir_paraProductos_consultaSoloElRepositorioDePedidos(TipoComprobanteEnum tipo) {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante(tipo.getSerie())).thenReturn("00000004");

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(tipo);

        assertThat(resultado.serie()).isEqualTo(tipo.getSerie());
        assertThat(resultado.numero()).isEqualTo("00000005");
        verify(pedidosRepository).ultimoNumeroComprobante(tipo.getSerie());
        verifyNoInteractions(ordenesRepository);
    }

    @ParameterizedTest
    @EnumSource(value = TipoComprobanteEnum.class, names = {"BOLETA_SERVICIO", "FACTURA_SERVICIO"})
    void emitir_paraServicios_consultaSoloElRepositorioDeOrdenes(TipoComprobanteEnum tipo) {
        stubBloqueo();
        when(ordenesRepository.ultimoNumeroComprobante(tipo.getSerie())).thenReturn("00000010");

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(tipo);

        assertThat(resultado.serie()).isEqualTo(tipo.getSerie());
        assertThat(resultado.numero()).isEqualTo("00000011");
        verify(ordenesRepository).ultimoNumeroComprobante(tipo.getSerie());
        verifyNoInteractions(pedidosRepository);
    }

    @ParameterizedTest
    @EnumSource(value = TipoComprobanteEnum.class, names = {"BOLETA_SERVICIO", "FACTURA_SERVICIO"})
    void emitir_paraServiciosSinComprobantesPrevios_empiezaEnUno(TipoComprobanteEnum tipo) {
        stubBloqueo();
        when(ordenesRepository.ultimoNumeroComprobante(tipo.getSerie())).thenReturn(null);

        assertThat(comprobanteService.emitir(tipo).completo()).isEqualTo(tipo.getSerie() + "-00000001");
    }

    @ParameterizedTest
    @EnumSource(TipoComprobanteEnum.class)
    void emitir_conCualquierTipo_usaLaSerieDelTipo(TipoComprobanteEnum tipo) {
        stubBloqueo();

        ComprobanteService.NumeroComprobante resultado = comprobanteService.emitir(tipo);

        assertThat(resultado.serie()).isEqualTo(tipo.getSerie());
        assertThat(resultado.numero()).isEqualTo("00000001");
    }

    // ---------- emitir: bloqueo para evitar números duplicados ----------

    @Test
    void emitir_tomaElBloqueoAntesDeLeerElUltimoNumero() {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("B001")).thenReturn("00000001");

        comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);

        InOrder orden = inOrder(entityManager, queryBloqueo, pedidosRepository);
        orden.verify(entityManager).createNativeQuery(anyString());
        orden.verify(queryBloqueo).setParameter(eq("clave"), any());
        orden.verify(queryBloqueo).getSingleResult();
        orden.verify(pedidosRepository).ultimoNumeroComprobante("B001");
    }

    @Test
    void emitir_usaUnBloqueoAdvisoryDePostgresPorSerie() {
        stubBloqueo();

        comprobanteService.emitir(TipoComprobanteEnum.FACTURA_SERVICIO);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(entityManager).createNativeQuery(sql.capture());
        assertThat(sql.getValue()).contains("pg_advisory_xact_lock");
        verify(queryBloqueo).setParameter("clave", (long) "comprobante:F002".hashCode());
    }

    @Test
    void emitir_conSeriesDistintas_usaClavesDeBloqueoDistintas() {
        stubBloqueo();

        comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);
        comprobanteService.emitir(TipoComprobanteEnum.FACTURA_PRODUCTO);

        verify(queryBloqueo).setParameter("clave", (long) "comprobante:B001".hashCode());
        verify(queryBloqueo).setParameter("clave", (long) "comprobante:F001".hashCode());
    }

    @Test
    void emitir_siFallaElBloqueo_propagaElErrorYNoLeeNingunNumero() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(queryBloqueo);
        when(queryBloqueo.setParameter(anyString(), any())).thenReturn(queryBloqueo);
        when(queryBloqueo.getSingleResult()).thenThrow(new IllegalStateException("sin conexión"));

        assertThatThrownBy(() -> comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("sin conexión");

        verifyNoInteractions(pedidosRepository, ordenesRepository);
    }

    @Test
    void emitir_dosVecesSeguidas_consultaElRepositorioEnCadaLlamada() {
        stubBloqueo();
        when(pedidosRepository.ultimoNumeroComprobante("B001")).thenReturn("00000001", "00000002");

        ComprobanteService.NumeroComprobante primero = comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);
        ComprobanteService.NumeroComprobante segundo = comprobanteService.emitir(TipoComprobanteEnum.BOLETA_PRODUCTO);

        assertThat(primero.numero()).isEqualTo("00000002");
        assertThat(segundo.numero()).isEqualTo("00000003");
    }

    // ---------- NumeroComprobante ----------

    @Test
    void numeroComprobante_completo_unePorGuion() {
        ComprobanteService.NumeroComprobante numero = new ComprobanteService.NumeroComprobante("F002", "00000123");

        assertThat(numero.completo()).isEqualTo("F002-00000123");
        assertThat(numero.serie()).isEqualTo("F002");
        assertThat(numero.numero()).isEqualTo("00000123");
    }
}