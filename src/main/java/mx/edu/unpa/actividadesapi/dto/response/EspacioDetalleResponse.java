package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EspacioDetalleResponse {

    private Integer idEspacio;
    private String nombre;
    private String descripcion;
    private Integer capacidad;
    private String ubicacion;
    private Boolean activo;

    // Ubicación interna (mapa UNPA) — nulo si es externo
    private Integer idPunto;
    private String etiquetaPunto;
    private BigDecimal coordX;
    private BigDecimal coordY;

    // Ubicación externa (Google Maps) — nulo si es interno
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String urlMaps;

    /** Indica si el espacio está en el mapa interno de la UNPA o es externo. */
    private Boolean esExterno;

    private List<EquipamientoResponse> equipamiento;
}
