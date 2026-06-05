package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.InscripcionExternoRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionExternoResponse;

/**
 * Contrato del servicio de inscripciones para externos (US-24).
 */
public interface InscripcionExternoService {

    /**
     * Inscribe a una persona externa en una actividad.
     *
     * @param idActividad identificador de la actividad
     * @param idVisitante UUID de la cookie HTTP-only (anti-duplicado)
     * @param request     datos del externo
     * @return confirmacion con los datos registrados y el total de externos
     */
    InscripcionExternoResponse inscribir(Integer idActividad,
                                         String idVisitante,
                                         InscripcionExternoRequest request);

    /**
     * Cancela la inscripcion de un externo en una actividad.
     * Solo es posible si el evento aun no ha iniciado.
     *
     * @param idActividad identificador de la actividad
     * @param idVisitante UUID de la cookie HTTP-only (identifica al externo)
     */
    void cancelar(Integer idActividad, String idVisitante);

    /**
     * Devuelve la cantidad de externos inscritos en una actividad.
     *
     * @param idActividad identificador de la actividad
     * @return total de externos inscritos
     */
    Integer totalExternos(Integer idActividad);

    /**
     * Indica si el visitante ya esta inscrito como externo en la actividad.
     *
     * @param idActividad identificador de la actividad
     * @param idVisitante UUID de la cookie HTTP-only
     * @return true si ya existe un registro para ese visitante en esa actividad
     */
    boolean estaInscrito(Integer idActividad, String idVisitante);
}