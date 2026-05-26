package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class InscripcionResponse {

    private Integer idInscripcion;
    private Integer idActividad;
    private String  nombreActividad;
    private String  categoria;
    private String  tipo;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String  campus;
    private String  imagenPortada;
    private LocalDateTime fechaInscripcion;
}
