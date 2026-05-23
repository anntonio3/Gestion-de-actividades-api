package mx.edu.unpa.actividadesapi.dto.response.vicerrectoria;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * US-07: Item del listado de solicitudes en el panel de admin.
 * Incluye lo necesario para la card sin cargar el detalle completo.
 */
@Data
public class SolicitudListItemResponse {
    private Integer idActividad;
    private String nombre;
    private String descripcion;
    private String nombreProfesor;
    private String correoProfesor;
    private String tipoActividad;
    private String campus; // nombre del campus
    private String categoria;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoActividad estado;
    private LocalDateTime fechaRegistro;
    // Etiquetas cortas de organizadores para mostrar en la card
    private List<String> organizadores;
    // Cantidad de recursos solicitados (resumen rapido)
    private Integer totalRecursos;
}