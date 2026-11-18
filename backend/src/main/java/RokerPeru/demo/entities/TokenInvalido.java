package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_invalidados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInvalido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false, unique = true)
    private String token;

    @Column(name = "fecha_invalidacion", nullable = false)
    private LocalDateTime fechaInvalidacion;

    public TokenInvalido(String token, LocalDateTime fechaInvalidacion) {
        this.token = token;
        this.fechaInvalidacion = fechaInvalidacion;
    }
}
