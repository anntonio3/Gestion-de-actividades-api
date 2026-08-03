package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO de detalle completo para vista pública del calendario.
 * Endpoint: GET /api/calendario/publico/{id}
 */
@Data
public class ActividadDetallePublicoResponse {

    // Datos básicos
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo;
    private String campus;
    private String categoria;
    private String imagenPortada;

    // Lugar (un solo espacio por actividad — regla de negocio)
    private LugarPublicoResponse lugar;

    // Organizadores (carreras y/o departamentos)
    private List<OrganizadorPublicoResponse> organizadores;

    // ===== US-29: indicador de cupo / aforo =====
    private Boolean requiereInscripcion;
    private Integer totalInscritos;

    // Lugares disponibles restantes (capacidad del lugar - totalInscritos, minimo 0).
    // Null si la actividad no tiene un espacio asignado o no define limite de aforo.
    private Integer lugaresDisponibles;

    // true cuando totalInscritos >= capacidad del lugar (y este define limite de aforo).
    private Boolean cupoLleno;

    // ===== Sub-DTOs internos =====

    @Data
    public static class LugarPublicoResponse {
        private Integer idEspacio;
        private String nombre;
        private String ubicacion;
        private Integer capacidad;

        // Ubicación interna (mapa UNPA) — nulo si el espacio es externo
        private Integer idPunto;
        private String etiquetaPunto;
        private BigDecimal coordX;
        private BigDecimal coordY;

        // Ubicación externa (Google Maps) — nulo si el espacio es interno
        private BigDecimal latitud;
        private BigDecimal longitud;

        /**
         * URL directa de Google Maps para el lugar externo.
         * Null si el espacio está en el mapa interno de la UNPA.
         * El frontend muestra un enlace "Ver en Google Maps" cuando este campo tiene valor.
         */
        private String urlMaps;

        /** True si el espacio es externo a la institución. */
        private Boolean esExterno;
    }

    @Data
    public static class OrganizadorPublicoResponse {
        private String nombre;
        private String tipo; // "CARRERA" o "DEPARTAMENTO"
    }
}