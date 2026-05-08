package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.edu.unpa.actividadesapi.enums.RespuestaAsistencia;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaResponse {
    private Integer idActividad;
    // Respuesta actual del visitante; null si nunca ha respondido
    private RespuestaAsistencia miRespuesta;
    private Integer totalVoy;
    private Integer totalTalVez;
    private Integer totalNoVoy;
}
