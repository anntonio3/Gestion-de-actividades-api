package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Integer idActividad;
    private String nombre;
    private String campus; // nombre del campus
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoActividad estado;
    private String motivoRechazo;
    private LocalDateTime fechaRegistro;
}