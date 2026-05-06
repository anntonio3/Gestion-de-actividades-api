package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AprobarActividadRequest {

    @NotNull(message = "El id del administrador es obligatorio")
    private Integer idAdmin;

    // Comentario opcional al aprobar (US-08)
    @Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
    private String comentario;
}
