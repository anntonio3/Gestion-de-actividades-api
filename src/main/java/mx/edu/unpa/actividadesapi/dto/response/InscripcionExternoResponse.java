package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.Sexo;

import java.time.LocalDateTime;

/**
 * US-24: Confirmacion de inscripcion de un externo.
 * Devuelve los datos registrados + el total acumulado de externos en la actividad.
 */
@Data
public class InscripcionExternoResponse {

    private Integer idInscripcionExterno;
    private Integer idActividad;
    private String nombreActividad;

    // Datos del externo confirmados
    private String nombre;
    private Integer edad;
    private Sexo sexo;
    private String procedencia;
    private String correo;
    private String telefono;

    private LocalDateTime fechaInscripcion;

    // Total de externos inscritos en esta actividad hasta este momento
    private Integer totalExternos;
}
