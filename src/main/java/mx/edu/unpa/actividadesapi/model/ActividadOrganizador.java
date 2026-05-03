package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "actividad_organizadores")
public class ActividadOrganizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    // Ambos son opcionales — al menos uno debe venir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = true)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento", nullable = true)
    private Departamento departamento;

}
