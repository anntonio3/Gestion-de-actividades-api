package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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
     * Punto del mapa interno de la UNPA.
     * Null cuando el espacio es externo a la institución.
     * Excluyente con latitud/longitud/urlMaps.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_punto", unique = true)
    private MapaPunto punto;

    /**
     * Latitud para espacios externos (Google Maps).
     * Null cuando el espacio pertenece al mapa interno.
     */
    @Column(name = "latitud", precision = 10, scale = 7)
    private BigDecimal latitud;

    /**
     * Longitud para espacios externos (Google Maps).
     * Null cuando el espacio pertenece al mapa interno.
     */
    @Column(name = "longitud", precision = 10, scale = 7)
    private BigDecimal longitud;

    /**
     * URL directa de Google Maps para el lugar externo.
     * El admin la copia desde Google Maps y la pega en el formulario.
     * Null cuando el espacio pertenece al mapa interno.
     */
    @Column(name = "url_maps", length = 1000)
    private String urlMaps;

    /**
     * Mobiliario fijo asignado al espacio.
     * Distinto del inventario global que vive en RecursoMobiliario.cantidad.
     */
    @OneToMany(mappedBy = "espacio", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EspacioEquipamiento> equipamiento = new ArrayList<>();

    // Helpers de negocio

    /** Devuelve true si este espacio está en el mapa interno de la UNPA. */
    public boolean esInterno() {
        return punto != null;
    }

    /** Devuelve true si este espacio tiene coordenadas externas (Google Maps). */
    public boolean esExterno() {
        return latitud != null && longitud != null;
    }
}
