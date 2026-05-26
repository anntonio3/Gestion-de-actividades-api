package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.InscripcionRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionEstadoResponse;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionResponse;

import java.util.List;
import java.util.Map;

public interface InscripcionService {

    // Inscribir al actor en la actividad
    InscripcionEstadoResponse inscribir(Integer idActividad, InscripcionRequest request);

    // Cancelar inscripcion (solo antes de que inicie el evento)
    void cancelar(Integer idActividad, InscripcionRequest request);

    // Listar inscripciones del actor
    List<InscripcionResponse> misInscripciones(Integer idActor, String tipoUsuario);

    // Estado de inscripcion del actor en una actividad
    InscripcionEstadoResponse obtenerEstado(Integer idActividad, Integer idActor, String tipoUsuario);

    // Estado en lote (para el calendario, evita N+1)
    Map<Integer, InscripcionEstadoResponse> obtenerEstadoEnLote(
            List<Integer> idsActividades, Integer idActor, String tipoUsuario);

    // Total de inscritos en una actividad (para mis-solicitudes del profesor)
    Integer totalInscritos(Integer idActividad);
}
