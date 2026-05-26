package mx.edu.unpa.actividadesapi.dto.response.estadisticas;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaCampusDTO {
    private Integer anio;
    private Integer mes;
    private Integer idDepartamento;
    private String  nombreDepartamento;
    private Long    cantidad;
}
