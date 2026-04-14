package mx.edu.unpa.actividadesapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Integer idActividad;
    private String nombre;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String motivoRechazo;
    private LocalDateTime fechaRegistro;
}