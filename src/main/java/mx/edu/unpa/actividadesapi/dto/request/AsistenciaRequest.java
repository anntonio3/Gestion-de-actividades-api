package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.RespuestaAsistencia;

@Data
public class AsistenciaRequest {

    @NotNull(message = "La respuesta es obligatoria")
    private RespuestaAsistencia respuesta;
}
