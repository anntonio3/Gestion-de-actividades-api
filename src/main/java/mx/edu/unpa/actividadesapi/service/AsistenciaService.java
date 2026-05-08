package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.response.AsistenciaResponse;
import mx.edu.unpa.actividadesapi.enums.RespuestaAsistencia;

import java.util.List;
import java.util.Map;

public interface AsistenciaService {

    /**
     * Garantiza la existencia de un visitante con ese id.
     * Si no existe lo crea. Idempotente.
     */
    void asegurarVisitante(String idVisitante);

    /**
     * Obtiene los conteos + la respuesta del visitante actual para una actividad.
     */
    AsistenciaResponse obtenerEstado(Integer idActividad, String idVisitante);

    /**
     * Obtiene los conteos para un lote de actividades.
     * Mapa idActividad -> AsistenciaResponse, sin incluir miRespuesta.
     */
    Map<Integer, AsistenciaResponse> obtenerConteosEnLote(
            List<Integer> idsActividades, String idVisitante);

    /**
     * Registra o actualiza la respuesta del visitante.
     */
    AsistenciaResponse responder(
            Integer idActividad, String idVisitante, RespuestaAsistencia respuesta);
}
