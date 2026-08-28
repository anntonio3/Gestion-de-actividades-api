package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class ActividadInscripcionResumenDTO {
    private Integer idActividad;
    private String nombreEvento;
    private String nombreProfesor;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer totalInscritos;
}
