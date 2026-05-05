package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para pintar cada círculo del mapa.
 * Si idEspacio es null, el punto está vacío (sin espacio asignado).
 */
@Data
public class MapaPuntoResponse {

    private Integer idPunto;
    private String etiqueta;
    private BigDecimal coordX;
    private BigDecimal coordY;

    // Datos del espacio (null si el punto no tiene espacio asignado)
    private Integer idEspacio;
    private String nombreEspacio;
    private Integer capacidad;
    private Boolean activo;

    public MapaPuntoResponse(Integer idPunto, String etiqueta,
                             BigDecimal coordX, BigDecimal coordY,
                             Integer idEspacio, String nombreEspacio,
                             Integer capacidad, Boolean activo) {
        this.idPunto = idPunto;
        this.etiqueta = etiqueta;
        this.coordX = coordX;
        this.coordY = coordY;
        this.idEspacio = idEspacio;
        this.nombreEspacio = nombreEspacio;
        this.capacidad = capacidad;
        this.activo = activo;
    }
}
