package mx.edu.unpa.actividadesapi.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ActividadPublicaDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo;       // nombre del tipo: "Académica", "Cultural"...
}