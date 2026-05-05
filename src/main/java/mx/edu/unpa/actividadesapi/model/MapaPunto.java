package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Representa un círculo sobre la imagen del mapa de la UNPA.
 * Las coordenadas son porcentajes (0-100) para posicionamiento responsive.
 * Existe independiente de RecursoEspacio: un punto puede no tener
 * espacio asignado todavía (estado "vacío" en la UI).
 */
@Data
@Entity
@Table(name = "mapa_puntos")
public class MapaPunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_punto")
    private Integer idPunto;

    @Column(name = "etiqueta", nullable = false, length = 20, unique = true)
    private String etiqueta;

    @Column(name = "coord_x", nullable = false, precision = 5, scale = 2)
    private BigDecimal coordX;

    @Column(name = "coord_y", nullable = false, precision = 5, scale = 2)
    private BigDecimal coordY;
}
