package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EspacioEstadoRequest {

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}
