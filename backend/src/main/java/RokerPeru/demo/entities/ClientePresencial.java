package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cliente de mostrador (venta presencial): NO es un usuario del sistema.
// No tiene correo, contraseña, rol ni dirección: solo el DNI que se imprime en la boleta.
@Entity
@Table(name = "clientes_presenciales")
@Getter
@Setter
@NoArgsConstructor
public class ClientePresencial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_presencial")
    private Long idClientePresencial;

    @Column(name = "dni_cliente_presencial", nullable = false, unique = true, length = 8)
    private String dniClientePresencial;
}