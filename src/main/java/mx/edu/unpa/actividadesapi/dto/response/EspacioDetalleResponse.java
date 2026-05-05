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

    // Datos del punto del mapa
    private Integer idPunto;
    private String etiquetaPunto;
    private BigDecimal coordX;
    private BigDecimal coordY;

    private List<EquipamientoResponse> equipamiento;
}
