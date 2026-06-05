package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.request.InscripcionExternoRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionExternoResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.InscripcionExterno;
import mx.edu.unpa.actividadesapi.model.Visitante;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.repository.InscripcionExternoRepository;
import mx.edu.unpa.actividadesapi.repository.VisitanteRepository;
import mx.edu.unpa.actividadesapi.service.InscripcionExternoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementacion del servicio de inscripciones para externos (US-24).
 */
@Service
public class InscripcionExternoServiceImpl implements InscripcionExternoService {

    private static final Logger log =
            LoggerFactory.getLogger(InscripcionExternoServiceImpl.class);

    @Autowired
    private InscripcionExternoRepository externoRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private VisitanteRepository visitanteRepository;

    // ====================================================================
    // Inscribir
    // ====================================================================

    @Override
    @Transactional
    public InscripcionExternoResponse inscribir(Integer idActividad,
                                                String idVisitante,
                                                InscripcionExternoRequest request) {
        log.info("Inscripcion de externo en actividad id={} visitante={} nombre='{}'",
                idActividad, idVisitante, request.getNombre());

        Actividad actividad = obtenerActividadInscribible(idActividad);

        // Verificar que el evento no haya iniciado
        verificarEventoNoIniciado(actividad);

        // Anti-duplicado por visitante
        if (idVisitante != null && !idVisitante.isBlank()) {
            if (externoRepository.existsByActividad_IdActividadAndVisitante_IdVisitante(
                    idActividad, idVisitante)) {
                log.warn("Visitante {} ya inscrito como externo en actividad {}",
                        idVisitante, idActividad);
                throw new BusinessException(
                        "Ya te has inscrito previamente en esta actividad.");
            }
        }

        // Anti-duplicado por correo
        String correoNormalizado = normalizarCorreo(request.getCorreo());
        if (correoNormalizado != null) {
            externoRepository
                    .findByActividad_IdActividadAndCorreo(idActividad, correoNormalizado)
                    .ifPresent(e -> {
                        log.warn("Correo duplicado en inscripcion externo correo={} actividad={}",
                                correoNormalizado, idActividad);
                        throw new BusinessException(
                                "Ya existe una inscripcion con ese correo en esta actividad.");
                    });
        }

        // Asegurar visitante en BD
        Visitante visitante = null;
        if (idVisitante != null && !idVisitante.isBlank()) {
            visitante = asegurarVisitante(idVisitante);
        }

        InscripcionExterno externo = new InscripcionExterno();
        externo.setActividad(actividad);
        externo.setVisitante(visitante);
        externo.setNombre(request.getNombre().trim());
        externo.setEdad(request.getEdad());
        externo.setSexo(request.getSexo());
        externo.setProcedencia(request.getProcedencia().trim());
        externo.setCorreo(correoNormalizado);
        externo.setTelefono(normalizarTelefono(request.getTelefono()));

        InscripcionExterno guardado = externoRepository.save(externo);
        int total = externoRepository.countByActividad_IdActividad(idActividad);

        log.info("Externo inscrito id={} en actividad id={}, total externos={}",
                guardado.getIdInscripcionExterno(), idActividad, total);

        return toResponse(guardado, total);
    }

    // ====================================================================
    // Cancelar
    // ====================================================================

    @Override
    @Transactional
    public void cancelar(Integer idActividad, String idVisitante) {
        log.info("Cancelacion de inscripcion externo actividad={} visitante={}",
                idActividad, idVisitante);

        if (idVisitante == null || idVisitante.isBlank()) {
            throw new BusinessException(
                    "No se puede identificar al visitante para cancelar la inscripcion.");
        }

        // Verificar que la actividad exista
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        // Solo se puede cancelar antes de que inicie el evento
        verificarEventoNoIniciado(actividad);

        // Buscar la inscripcion del visitante
        InscripcionExterno inscripcion = externoRepository
                .findByActividad_IdActividadAndVisitante_IdVisitante(idActividad, idVisitante)
                .orElseThrow(() -> {
                    log.warn("No se encontro inscripcion externo para cancelar actividad={} visitante={}",
                            idActividad, idVisitante);
                    return new ResourceNotFoundException(
                            "No se encontro una inscripcion activa para cancelar.");
                });

        externoRepository.delete(inscripcion);
        log.info("Inscripcion externo cancelada id={} actividad={} visitante={}",
                inscripcion.getIdInscripcionExterno(), idActividad, idVisitante);
    }

    // ====================================================================
    // Total de externos
    // ====================================================================

    @Override
    @Transactional(readOnly = true)
    public Integer totalExternos(Integer idActividad) {
        return externoRepository.countByActividad_IdActividad(idActividad);
    }

    // ====================================================================
    // Consulta de estado
    // ====================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean estaInscrito(Integer idActividad, String idVisitante) {
        if (idVisitante == null || idVisitante.isBlank()) return false;
        return externoRepository.existsByActividad_IdActividadAndVisitante_IdVisitante(
                idActividad, idVisitante);
    }

    // ====================================================================
    // Helpers privados
    // ====================================================================

    /**
     * Obtiene la actividad y valida que este APROBADA y requiera inscripcion.
     */
    private Actividad obtenerActividadInscribible(Integer idActividad) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        if (actividad.getEstado() != EstadoActividad.APROBADA) {
            throw new BusinessException("Solo se puede inscribir en actividades aprobadas.");
        }
        if (!Boolean.TRUE.equals(actividad.getRequiereInscripcion())) {
            throw new BusinessException("Esta actividad no requiere inscripcion formal.");
        }
        return actividad;
    }

    /**
     * Lanza excepcion si el evento ya inicio.
     * Se reutiliza tanto en inscribir() como en cancelar().
     */
    private void verificarEventoNoIniciado(Actividad actividad) {
        LocalDateTime inicioEvento = actividad.getFechaActividad()
                .atTime(actividad.getHoraInicio());
        if (LocalDateTime.now().isAfter(inicioEvento)) {
            throw new BusinessException(
                    "El periodo de inscripcion para esta actividad ha cerrado.");
        }
    }

    /**
     * Garantiza que el visitante exista en la tabla visitantes.
     * Si no existe lo crea (mismo patron que AsistenciaServiceImpl).
     */
    private Visitante asegurarVisitante(String idVisitante) {
        return visitanteRepository.findById(idVisitante).orElseGet(() -> {
            Visitante nuevo = new Visitante();
            nuevo.setIdVisitante(idVisitante);
            Visitante guardado = visitanteRepository.save(nuevo);
            log.info("Nuevo visitante creado para inscripcion externo id={}", idVisitante);
            return guardado;
        });
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) return null;
        return correo.trim().toLowerCase();
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) return null;
        return telefono.trim();
    }

    private InscripcionExternoResponse toResponse(InscripcionExterno e, int total) {
        InscripcionExternoResponse dto = new InscripcionExternoResponse();
        dto.setIdInscripcionExterno(e.getIdInscripcionExterno());
        dto.setIdActividad(e.getActividad().getIdActividad());
        dto.setNombreActividad(e.getActividad().getNombre());
        dto.setNombre(e.getNombre());
        dto.setEdad(e.getEdad());
        dto.setSexo(e.getSexo());
        dto.setProcedencia(e.getProcedencia());
        dto.setCorreo(e.getCorreo());
        dto.setTelefono(e.getTelefono());
        dto.setFechaInscripcion(e.getFechaInscripcion());
        dto.setTotalExternos(total);
        return dto;
    }
}
