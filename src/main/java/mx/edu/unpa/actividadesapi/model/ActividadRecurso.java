package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "actividad_recursos")
@IdClass(ActividadRecursoId.class)
public class ActividadRecurso {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_recurso", nullable = false)
    private Recurso recurso;

    @Column(name = "cantidad_requerida", nullable = false)
    private Integer cantidadRequerida = 1;
}