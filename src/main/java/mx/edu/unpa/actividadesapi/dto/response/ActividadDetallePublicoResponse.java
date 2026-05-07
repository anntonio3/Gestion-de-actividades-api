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

    // Datos basicos
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo;
    private String categoria;
    private String imagenPortada;

    // Lugar (un solo espacio por actividad - regla de negocio)
    private LugarPublicoResponse lugar;

    // Organizadores (carreras y/o departamentos)
    private List<OrganizadorPublicoResponse> organizadores;

    // ===== Sub-DTOs internos =====

    @Data
    public static class LugarPublicoResponse {
        private Integer idEspacio;
        private String nombre;
        private String ubicacion;
        private Integer capacidad;

        // Coordenadas para el mapa (null si el espacio no esta anclado a un punto)
        private Integer idPunto;
        private String etiquetaPunto;
        private BigDecimal coordX;
        private BigDecimal coordY;
    }

    @Data
    public static class OrganizadorPublicoResponse {
        private String nombre;
        private String tipo;  // "CARRERA" o "DEPARTAMENTO"
    }
}