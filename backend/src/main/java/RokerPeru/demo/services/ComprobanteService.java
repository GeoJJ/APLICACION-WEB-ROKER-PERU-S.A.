package RokerPeru.demo.services;

import RokerPeru.demo.entities.SecuenciaComprobante;
import RokerPeru.demo.entities.Usuario;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.SecuenciaComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final SecuenciaComprobanteRepository secuenciaRepository;

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

    @Transactional
    public NumeroComprobante emitir(TipoComprobanteEnum tipo) {
        String serie = tipo.getSerie();

        SecuenciaComprobante secuencia = secuenciaRepository.findBySerieParaActualizar(serie)
                .orElseGet(() -> secuenciaRepository.save(new SecuenciaComprobante(serie, 0)));

        int siguiente = secuencia.getUltimoNumero() + 1;
        secuencia.setUltimoNumero(siguiente);

        return new NumeroComprobante(serie, String.format("%08d", siguiente));
    }
}