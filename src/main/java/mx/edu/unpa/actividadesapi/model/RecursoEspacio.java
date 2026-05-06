package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "recursos_espacio")
@PrimaryKeyJoinColumn(name = "id_recurso")
@DiscriminatorValue("ESPACIO")
public class RecursoEspacio extends Recurso {

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "ubicacion", nullable = false, length = 150)
    private String ubicacion;

    /**
     * Punto del mapa donde se ubica el espacio.
     * Puede ser null si el espacio existe pero aún no se ha
     * anclado al mapa (datos legacy o futura migración).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_punto", unique = true)
    private MapaPunto punto;

    /**
     * Mobiliario fijo asignado al espacio (sillas, proyectores, etc.).
     * Distinto del inventario global que vive en RecursoMobiliario.cantidad.
     */
    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EspacioEquipamiento> equipamiento = new ArrayList<>();

}
