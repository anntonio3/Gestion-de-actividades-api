package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "campus")
public class Campus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campus")
    private Integer idCampus;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;
}
