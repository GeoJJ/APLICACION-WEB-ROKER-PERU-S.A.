package RokerPeru.demo.services;

import RokerPeru.demo.entities.SecuenciaComprobante;
import RokerPeru.demo.enums.TipoComprobanteEnum;
import RokerPeru.demo.repository.SecuenciaComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final SecuenciaComprobanteRepository secuenciaRepository;

    //Genera un número de comprobante automático.
    @Transactional
    public String generarNumeroComprobante(TipoComprobanteEnum tipo) {
        String serie = tipo.getSerie();

        // Buscamos si ya existe la secuencia para esta serie
        SecuenciaComprobante secuencia = secuenciaRepository.findBySerie(serie)
                .orElseGet(() -> {
                    // Si no existe (es la primera vez), la creamos empezando en 0
                    SecuenciaComprobante nueva = new SecuenciaComprobante(serie, 0);
                    return secuenciaRepository.save(nueva);
                });

        // Le decimos a la BD que sume 1 al número actual
        secuencia.setUltimoNumero(secuencia.getUltimoNumero()+1);

        // Volvemos a leer el número actualizado
        SecuenciaComprobante secuenciaActualizada = secuencia;
        int nuevoNumero = secuenciaActualizada.getUltimoNumero();

        // Formateamos: "B002" + "-" + "00000001" (8 dígitos con ceros a la izquierda)
        String numeroFormateado = String.format("%08d", nuevoNumero);

        return serie + "-" + numeroFormateado;
    }
}
