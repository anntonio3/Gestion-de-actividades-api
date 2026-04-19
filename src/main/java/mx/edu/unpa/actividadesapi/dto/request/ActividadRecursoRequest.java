package mx.edu.unpa.actividadesapi.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Data
public class ActividadRecursoRequest {

    @NotNull(message = "El id del recurso es obligatorio")
    private Integer idRecurso;

    @Min(value = 1, message = "La cantidad requerida debe ser al menos 1")
    private Integer cantidadRequerida = 1;
}
