package mx.edu.unpa.actividadesapi.dto.response.estadisticas;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadística de actividades desglosada por carrera.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaCarreraDTO {
    private Integer anio;
    private Integer mes;
    private Integer idCarrera;
    private String  nombreCarrera;
    private Long    cantidad;
}
