package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipamientoRequest {

    @NotNull(message = "El id del recurso es obligatorio")
    private Integer idRecurso;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Size(max = 300, message = "Las características no deben exceder 300 caracteres")
    private String caracteristicas;
}
