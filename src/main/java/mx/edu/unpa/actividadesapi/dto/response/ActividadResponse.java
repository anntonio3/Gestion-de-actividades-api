package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ActividadResponse {

    private Integer idActividad;
    private String nombreProfesor;
    private String tipoActividad;
    private String campus; // nombre del campus
    private String categoria;
    private String nombre;
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoActividad estado;
    private LocalDateTime fechaRegistro;
    private List<RecursoResumen> recursos;

    private String urlPortada;
    private List<String> organizadores; // ej. ["Rectoria", "Ingeniería en Computacion"]

    // Clase interna para resumir recursos
    @Data
    public static class RecursoResumen {
        private Integer idRecurso;
        private String nombre;
        private String tipoRecurso;
        private Integer cantidadRequerida;
    }
}
