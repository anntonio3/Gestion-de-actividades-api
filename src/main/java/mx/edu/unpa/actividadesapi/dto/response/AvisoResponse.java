package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * US-17/US-18/US-19/US-20: Datos del aviso para enviar al frontend.
 * Mismo formato para vista pública y panel del profesor.
 */
@Data
public class AvisoResponse {

    private Integer idAviso;
    private String titulo;
    private String descripcion;
    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private String fotoUrl;

    // Profesor que publica (para que el corcho muestre "por: Carlos Hernandez")
    private Integer idProfesor;
    private String nombreProfesor;

    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaActualizacion;
}
