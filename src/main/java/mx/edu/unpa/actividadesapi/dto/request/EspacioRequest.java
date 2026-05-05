package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EspacioRequest {

    @NotNull(message = "El punto del mapa es obligatorio")
    private Integer idPunto;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String descripcion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidad;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no debe exceder 150 caracteres")
    private String ubicacion;

    /** Lista de equipamiento opcional. Puede venir vacía. */
    @Valid
    private List<EquipamientoRequest> equipamiento = new ArrayList<>();
}
