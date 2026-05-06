package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RechazarActividadRequest {

    @NotNull(message = "El id del administrador es obligatorio")
    private Integer idAdmin;

    // US-09: motivo de rechazo obligatorio
    @NotBlank(message = "El motivo de rechazo es obligatorio")
    @Size(min = 5, max = 1000, message = "El motivo debe tener entre 5 y 1000 caracteres")
    private String motivo;
}
