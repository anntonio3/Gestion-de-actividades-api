package mx.edu.unpa.actividadesapi.dto.response.estadisticas;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaMesDTO {
    private Integer anio;
    private Integer mes;
    private Long    cantidad;
}
