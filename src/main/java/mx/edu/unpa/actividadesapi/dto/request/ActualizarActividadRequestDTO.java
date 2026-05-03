package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mx.edu.unpa.actividadesapi.model.TipoActividad;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActualizarActividadRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La fecha de la actividad es requerida")
    private LocalDate fechaActividad;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es requerida")
    private LocalTime horaFin;

    private TipoActividad idTipo;
}
