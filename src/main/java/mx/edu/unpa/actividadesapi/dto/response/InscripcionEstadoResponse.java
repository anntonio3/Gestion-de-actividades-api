package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estado de inscripcion del usuario actual en una actividad.
 * Se devuelve junto con el total de inscritos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionEstadoResponse {

    private Integer idActividad;
    private Boolean inscrito;          // true si el actor ya esta inscrito
    private Integer idInscripcion;     // null si no esta inscrito
    private Integer totalInscritos;

    private Integer aforo;

    private Integer lugaresDisponibles;

    private Boolean cupoLleno = false;

    public InscripcionEstadoResponse(Integer idActividad, Boolean inscrito,
                                     Integer idInscripcion, Integer totalInscritos) {
        this.idActividad = idActividad;
        this.inscrito = inscrito;
        this.idInscripcion = idInscripcion;
        this.totalInscritos = totalInscritos;
    }
}
