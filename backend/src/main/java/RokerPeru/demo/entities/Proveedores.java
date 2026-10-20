package RokerPeru.demo.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
public class Proveedores {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long idProveedor;

    @Column(name = "nombre_proveedor", nullable = false, length = 150)
    private String nombreProveedor;

    @Column(name = "ruc_proveedor", unique = true, length = 11)
    private String rucProveedor;

    @Column(name = "telefono_proveedor", length = 20)
    private String telefonoProveedor;

    @Column(name = "correo_proveedor", length = 150)
    private String correoProveedor;

    @OneToMany(mappedBy = "proveedor")
    private List<Producto> productos = new ArrayList<>();
}