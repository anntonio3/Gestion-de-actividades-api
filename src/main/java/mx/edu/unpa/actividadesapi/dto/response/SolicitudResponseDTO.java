package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SolicitudResponseDTO {
    private Integer idActividad;
    private String nombre;
    private String campus;
    private String descripcion;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoActividad estado;
    private String motivoRechazo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    // Campos adicionales para que el profesor vea todo lo que registró
    private String tipoActividad;
    private String categoria;
    private List<String> organizadores;
    private List<RecursoResumenDTO> recursos;
    private List<ImagenDTO> imagenes;

    // NUEVO (US-28): indica si el evento requiere inscripcion formal,
    // usado por el frontend para mostrar el boton "Ver inscritos"
    private Boolean requiereInscripcion;

    @Data
    @AllArgsConstructor
    public static class RecursoResumenDTO {
        private Integer idRecurso;
        private String nombre;
        private String tipoRecurso;
        private Integer cantidadRequerida;
    }

    @Data
    @AllArgsConstructor
    public static class ImagenDTO {
        private Integer idImagen;
        private String url;
        private String nombreArchivo;
        private Boolean esPortada;
        private LocalDateTime fechaSubida;
    }
}
