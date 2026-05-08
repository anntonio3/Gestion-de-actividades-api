package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.response.AsistenciaResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.enums.RespuestaAsistencia;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.ActividadAsistencia;
import mx.edu.unpa.actividadesapi.model.Visitante;
import mx.edu.unpa.actividadesapi.repository.ActividadAsistenciaRepository;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.repository.VisitanteRepository;
import mx.edu.unpa.actividadesapi.service.AsistenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaServiceImpl.class);

    @Autowired private VisitanteRepository visitanteRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private ActividadAsistenciaRepository asistenciaRepository;

    // ====================================================================
    // Visitante
    // ====================================================================
    @Override
    @Transactional
    public void asegurarVisitante(String idVisitante) {
        if (idVisitante == null || idVisitante.isBlank()) {
            throw new BusinessException("Id de visitante invalido");
        }
        if (!visitanteRepository.existsById(idVisitante)) {
            Visitante v = new Visitante();
            v.setIdVisitante(idVisitante);
            visitanteRepository.save(v);
            log.info("Nuevo visitante registrado id={}", idVisitante);
        }
    }

    // ====================================================================
    // Consultas
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public AsistenciaResponse obtenerEstado(Integer idActividad, String idVisitante) {
        // Validamos solo que la actividad exista; no exigimos APROBADA porque
        // el calendario solo muestra aprobadas, y filtrar otra vez aqui seria redundante.
        if (!actividadRepository.existsById(idActividad)) {
            throw new ResourceNotFoundException("Actividad no encontrada con id: " + idActividad);
        }

        AsistenciaResponse dto = construirConteo(idActividad);

        if (idVisitante != null && !idVisitante.isBlank()) {
            asistenciaRepository
                    .findByVisitante_IdVisitanteAndActividad_IdActividad(idVisitante, idActividad)
                    .ifPresent(a -> dto.setMiRespuesta(a.getRespuesta()));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, AsistenciaResponse> obtenerConteosEnLote(
            List<Integer> ids, String idVisitante) {

        if (ids == null || ids.isEmpty()) return Map.of();

        // Inicializa todos en cero para devolver entrada por cada id solicitado.
        Map<Integer, AsistenciaResponse> mapa = new HashMap<>();
        for (Integer id : ids) {
            mapa.put(id, new AsistenciaResponse(id, null, 0, 0, 0));
        }

        for (Object[] row : asistenciaRepository.contarPorRespuestaEnLote(ids)) {
            Integer idAct = (Integer) row[0];
            RespuestaAsistencia r = (RespuestaAsistencia) row[1];
            int total = ((Number) row[2]).intValue();
            asignarConteo(mapa.get(idAct), r, total);
        }

        // Marca la respuesta del visitante en las que haya votado
        if (idVisitante != null && !idVisitante.isBlank()) {
            // Aqui podria optimizarse con un IN-query, pero el numero de cards
            // por pagina es chico (~8) y consultar una a una mantiene el codigo simple.
            for (Integer idAct : ids) {
                asistenciaRepository
                        .findByVisitante_IdVisitanteAndActividad_IdActividad(idVisitante, idAct)
                        .ifPresent(a -> mapa.get(idAct).setMiRespuesta(a.getRespuesta()));
            }
        }
        return mapa;
    }

    // ====================================================================
    // Registro / actualizacion
    // ====================================================================
    @Override
    @Transactional
    public AsistenciaResponse responder(Integer idActividad, String idVisitante,
                                        RespuestaAsistencia respuesta) {
        if (respuesta == null) {
            throw new BusinessException("La respuesta es obligatoria");
        }
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        // Reglas de negocio
        if (actividad.getEstado() != EstadoActividad.APROBADA) {
            throw new BusinessException("Solo se puede responder a actividades aprobadas");
        }
        if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
            throw new BusinessException("Esta actividad ya finalizo");
        }
        // Si en US-11 se activa requiereInscripcion=true, US-12 no aplica.
        if (Boolean.TRUE.equals(actividad.getRequiereInscripcion())) {
            throw new BusinessException(
                    "Esta actividad requiere inscripcion formal, no encuesta de asistencia");
        }

        asegurarVisitante(idVisitante);
        Visitante visitante = visitanteRepository.getReferenceById(idVisitante);

        // Upsert: si ya respondio, actualiza; si no, crea
        ActividadAsistencia asistencia = asistenciaRepository
                .findByVisitante_IdVisitanteAndActividad_IdActividad(idVisitante, idActividad)
                .orElseGet(() -> {
                    ActividadAsistencia nueva = new ActividadAsistencia();
                    nueva.setVisitante(visitante);
                    nueva.setActividad(actividad);
                    return nueva;
                });
        asistencia.setRespuesta(respuesta);
        asistenciaRepository.save(asistencia);

        log.info("Visitante {} respondio {} para actividad id={}",
                idVisitante, respuesta, idActividad);

        AsistenciaResponse dto = construirConteo(idActividad);
        dto.setMiRespuesta(respuesta);
        return dto;
    }

    // ====================================================================
    // Helpers
    // ====================================================================
    private AsistenciaResponse construirConteo(Integer idActividad) {
        AsistenciaResponse dto = new AsistenciaResponse(idActividad, null, 0, 0, 0);
        for (Object[] row : asistenciaRepository.contarPorRespuesta(idActividad)) {
            asignarConteo(dto, (RespuestaAsistencia) row[0], ((Number) row[1]).intValue());
        }
        return dto;
    }

    private void asignarConteo(AsistenciaResponse dto, RespuestaAsistencia r, int total) {
        switch (r) {
            case VOY -> dto.setTotalVoy(total);
            case TAL_VEZ -> dto.setTotalTalVez(total);
            case NO_VOY -> dto.setTotalNoVoy(total);
        }
    }
}