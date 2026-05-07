package mx.edu.unpa.actividadesapi.dto.response.vicerrectoria;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * US-10: Detalle completo de una solicitud para tomar decision.
 * Incluye conflictos detectados con actividades ya aprobadas.
 */
@Data
public class SolicitudDetalleResponse {

    private Integer idActividad;
    private String nombre;
    private String descripcion;
    private String nombreProfesor;
    private String correoProfesor;
    private String tipoActividad;
    private String categoria;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoActividad estado;
    private LocalDateTime fechaRegistro;

    // Datos de revision (si ya fue decidida)
    private String motivoRechazo;
    private LocalDateTime fechaRevision;
    private String nombreVicerrector;

    private String urlPortada;
    private List<String> organizadores;
    private List<RecursoDetalle> recursos;

    // US-10: lista de conflictos detectados
    private List<ConflictoRecurso> conflictos;

    // Token de concurrencia para optimistic locking
    private Integer version;

    @Data
    public static class RecursoDetalle {
        private Integer idRecurso;
        private String nombre;
        private String tipoRecurso; // ESPACIO, MOBILIARIO, PERSONAL
        private Integer cantidadRequerida;
        // Para espacios: capacidad y ubicacion (informativo)
        private Integer capacidad;
        private String ubicacion;
        // Para mobiliario: total en inventario
        private Integer cantidadInventario;
    }

    @Data
    public static class ConflictoRecurso {
        private Integer idRecurso;
        private String nombreRecurso;
        private String tipoRecurso;
        private String mensaje;
        // Para mobiliario: cuanto se pidio vs cuanto queda
        private Integer cantidadSolicitada;
        private Integer cantidadDisponible;
    }
}
