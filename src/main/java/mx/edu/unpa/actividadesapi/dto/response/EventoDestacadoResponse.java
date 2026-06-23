package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * US-27: Datos del evento destacado para el banner publico del calendario.
 * Liviano: solo lo necesario para pintar el banner y enlazar al detalle.
 */
@Data
public class EventoDestacadoResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String categoria;
    private String tipo;
    private String imagenPortada;

    // Lugar (nombre del espacio si lo tiene) para mostrar en el banner
    private String lugar;
    private Integer capacidad;
}
