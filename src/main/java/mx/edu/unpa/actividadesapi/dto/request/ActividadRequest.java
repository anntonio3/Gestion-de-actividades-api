package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class ActividadRequest {

    @NotNull(message = "El id del profesor es obligatorio")
    private Integer idProfesor;

    @NotNull(message = "El tipo de actividad es obligatorio")
    private Integer idTipo;

    //@NotNull(message = "El campus es obligatorio")
    private Integer idCampus;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @Size(max = 5000, message = "La descripción es demasiado larga")
    private String descripcion;

    @NotNull(message = "La fecha de la actividad es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fechaActividad;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;

    @NotEmpty(message = "Debe incluir al menos un recurso")
    @Valid
    private List<ActividadRecursoRequest> recursos;

    // Organizadores — al menos uno requerido
    @NotEmpty(message = "Debe incluir al menos un organizador")
    @Valid
    private List<OrganizadorRequest> organizadores;

    // Clase interna para el request de organizadores
    @Data
    public static class OrganizadorRequest {
        private Integer idCarrera;       // nullable
        private Integer idDepartamento;  // nullable
        // getters y setters
    }
}
