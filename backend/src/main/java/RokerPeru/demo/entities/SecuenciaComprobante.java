package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "secuencias_comprobantes")
@Getter
@Setter
@NoArgsConstructor
public class SecuenciaComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serie", nullable = false, unique = true, length = 10)
    private String serie; // Ej: "B001", "F002"

    @Column(name = "ultimo_numero", nullable = false)
    private int ultimoNumero; // Ej: 123

    public SecuenciaComprobante(String serie, int ultimoNumero) {
        this.serie = serie;
        this.ultimoNumero = ultimoNumero;
    }
}