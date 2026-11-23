package RokerPeru.demo.services;

import RokerPeru.demo.entities.SecuenciaComprobante;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.SecuenciaComprobanteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {

    @Mock
    private SecuenciaComprobanteRepository secuenciaRepository;

    @InjectMocks
    private ComprobanteService comprobanteService;

    private SecuenciaComprobante secuenciaConNumero(int numero) {
        SecuenciaComprobante secuencia = mock(SecuenciaComprobante.class);
        when(secuencia.getUltimoNumero()).thenReturn(numero);
        return secuencia;
    }

    @ParameterizedTest
    @EnumSource(TipoComprobanteEnum.class)
    void generarNumeroComprobante_conSecuenciaExistente_retornaSerieYNumeroSiguiente(TipoComprobanteEnum tipo) {
        String serie = tipo.getSerie();
        SecuenciaComprobante existente = mock(SecuenciaComprobante.class);
        SecuenciaComprobante actualizada = secuenciaConNumero(5);

        when(secuenciaRepository.findBySerie(serie))
                .thenReturn(Optional.of(existente), Optional.of(actualizada));

        String resultado = comprobanteService.generarNumeroComprobante(tipo);

        assertThat(resultado).isEqualTo(serie + "-00000005");
        verify(secuenciaRepository).incrementarNumero(serie);
        verify(secuenciaRepository, never()).save(any());
    }

    @Test
    void generarNumeroComprobante_cuandoNoExisteLaSecuencia_laCreaYGeneraElPrimerNumero() {
        TipoComprobanteEnum tipo = TipoComprobanteEnum.BOLETA_PRODUCTO;
        String serie = tipo.getSerie();
        SecuenciaComprobante nueva = mock(SecuenciaComprobante.class);
        SecuenciaComprobante actualizada = secuenciaConNumero(1);

        when(secuenciaRepository.findBySerie(serie))
                .thenReturn(Optional.empty(), Optional.of(actualizada));
        when(secuenciaRepository.save(any(SecuenciaComprobante.class))).thenReturn(nueva);

        String resultado = comprobanteService.generarNumeroComprobante(tipo);

        assertThat(resultado).isEqualTo(serie + "-00000001");
        verify(secuenciaRepository).save(any(SecuenciaComprobante.class));
        verify(secuenciaRepository).incrementarNumero(serie);
    }

    @Test
    void generarNumeroComprobante_rellenaConCerosHastaOchoDigitos() {
        TipoComprobanteEnum tipo = TipoComprobanteEnum.FACTURA_SERVICIO;
        String serie = tipo.getSerie();

        SecuenciaComprobante existente = mock(SecuenciaComprobante.class);
        SecuenciaComprobante actualizada = secuenciaConNumero(12345);

        when(secuenciaRepository.findBySerie(serie))
                .thenReturn(Optional.of(existente), Optional.of(actualizada));

        String resultado = comprobanteService.generarNumeroComprobante(tipo);

        assertThat(resultado).isEqualTo(serie + "-00012345");
        assertThat(resultado.substring(resultado.indexOf('-') + 1)).hasSize(8);
    }

    @Test
    void generarNumeroComprobante_incrementaAntesDeLeerElNumeroActualizado() {
        TipoComprobanteEnum tipo = TipoComprobanteEnum.BOLETA_SERVICIO;
        String serie = tipo.getSerie();

        SecuenciaComprobante existente = mock(SecuenciaComprobante.class);
        SecuenciaComprobante actualizada = secuenciaConNumero(2);

        when(secuenciaRepository.findBySerie(serie))
                .thenReturn(Optional.of(existente), Optional.of(actualizada));

        comprobanteService.generarNumeroComprobante(tipo);

        InOrder orden = inOrder(secuenciaRepository);
        orden.verify(secuenciaRepository).findBySerie(serie);
        orden.verify(secuenciaRepository).incrementarNumero(serie);
        orden.verify(secuenciaRepository).findBySerie(serie);
    }
}