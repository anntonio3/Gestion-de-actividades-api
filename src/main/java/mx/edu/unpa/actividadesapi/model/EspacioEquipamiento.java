package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Mobiliario fijo asignado a un espacio.
 * Mismo patrón que ActividadRecurso (PK compuesta con @IdClass).
 */
@Data
@Entity
@Table(name = "espacio_equipamiento")
@IdClass(EspacioEquipamientoId.class)
public class EspacioEquipamiento {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_espacio", nullable = false)
    private RecursoEspacio espacio;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "caracteristicas", length = 300)
    private String caracteristicas;
}
