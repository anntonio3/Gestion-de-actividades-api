package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.TipoUsuario;

@Data
public class InscripcionRequest {

    @NotNull(message = "El id del actor es obligatorio")
    private Integer idActor;

    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;   // ALUMNO, PROFESOR o ADMIN
}
