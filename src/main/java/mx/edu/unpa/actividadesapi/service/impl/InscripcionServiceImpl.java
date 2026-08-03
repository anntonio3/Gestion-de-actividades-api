package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.InscripcionRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionEstadoResponse;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.enums.TipoUsuario;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.InscripcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InscripcionServiceImpl implements InscripcionService {

    private static final Logger log = LoggerFactory.getLogger(InscripcionServiceImpl.class);

    private final InscripcionActividadRepository inscripcionRepository;
    private final ActividadRepository            actividadRepository;
    private final UsuarioRepository              usuarioRepository;
    private final AlumnoRepository               alumnoRepository;
    private final ActividadImagenRepository      imagenRepository;

    // Repositorio de externos: necesario para sumar su conteo en totalInscritos
    private final InscripcionExternoRepository   externoRepository;

    private final ActividadRecursoRepository     actividadRecursoRepository;
    private final RecursoEspacioRepository       recursoEspacioRepository;

    // ====================================================================
    // Inscribir
    // ====================================================================
    @Override
    @Transactional
    public InscripcionEstadoResponse inscribir(Integer idActividad, InscripcionRequest request) {
        log.info("Inscripcion solicitada: actividad={} actor={} tipo={}",
                idActividad, request.getIdActor(), request.getTipoUsuario());

        Actividad actividad = obtenerActividadInscribible(idActividad);
        boolean esAlumno = request.getTipoUsuario() == TipoUsuario.ALUMNO;

        verificarNoInscrito(idActividad, request.getIdActor(), esAlumno);
        verificarSinConflictoHorario(actividad, request.getIdActor(), esAlumno);
        verificarCupoDisponible(idActividad);


        InscripcionActividad inscripcion = new InscripcionActividad();
        inscripcion.setActividad(actividad);

        if (esAlumno) {
            Alumno alumno = alumnoRepository.findById(request.getIdActor())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Alumno no encontrado con id: " + request.getIdActor()));
            inscripcion.setAlumno(alumno);
        } else {
            Usuario usuario = usuarioRepository.findById(request.getIdActor())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con id: " + request.getIdActor()));
            inscripcion.setUsuario(usuario);
        }

        InscripcionActividad guardada = inscripcionRepository.save(inscripcion);

        // Suma inscritos del sistema + externos para el conteo unificado

        int total = conteoTotal(idActividad);

        log.info("Inscripcion registrada id={} en actividad={}", guardada.getIdInscripcion(), idActividad);
        InscripcionEstadoResponse estado = new InscripcionEstadoResponse(idActividad, true, guardada.getIdInscripcion(), total);
        aplicarCupo(estado, idActividad);
        return estado;

    }

    // ====================================================================
    // Cancelar
    // ====================================================================
    @Override
    @Transactional
    public void cancelar(Integer idActividad, InscripcionRequest request) {
        log.info("Cancelacion de inscripcion: actividad={} actor={} tipo={}",
                idActividad, request.getIdActor(), request.getTipoUsuario());

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        LocalDateTime inicioEvento = actividad.getFechaActividad()
                .atTime(actividad.getHoraInicio());
        if (LocalDateTime.now().isAfter(inicioEvento)) {
            log.warn("Intento de cancelar inscripcion de actividad ya iniciada id={}", idActividad);
            throw new BusinessException("No es posible cancelar la inscripcion de un evento que ya inicio.");
        }

        boolean esAlumno = request.getTipoUsuario() == TipoUsuario.ALUMNO;
        InscripcionActividad inscripcion = buscarInscripcion(idActividad, request.getIdActor(), esAlumno);

        inscripcionRepository.delete(inscripcion);
        log.info("Inscripcion cancelada: actividad={} actor={}", idActividad, request.getIdActor());
    }

    // ====================================================================
    // Mis inscripciones
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public List<InscripcionResponse> misInscripciones(Integer idActor, String tipoUsuario) {
        log.info("Listando inscripciones actor={} tipo={}", idActor, tipoUsuario);

        boolean esAlumno = "ALUMNO".equalsIgnoreCase(tipoUsuario);
        List<InscripcionActividad> lista = esAlumno
                ? inscripcionRepository.findByAlumno_IdAlumnoOrderByFechaInscripcionDesc(idActor)
                : inscripcionRepository.findByUsuario_IdUsuarioOrderByFechaInscripcionDesc(idActor);

        return lista.stream().map(this::toResponse).toList();
    }

    // ====================================================================
    // Estado de inscripcion
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public InscripcionEstadoResponse obtenerEstado(Integer idActividad, Integer idActor, String tipoUsuario) {
        boolean esAlumno = "ALUMNO".equalsIgnoreCase(tipoUsuario);
        Optional<InscripcionActividad> opt = buscarInscripcionOpcional(idActividad, idActor, esAlumno);

        // Conteo unificado: usuarios del sistema + externos
        int total = conteoTotal(idActividad);

        InscripcionEstadoResponse estado = opt.map(i -> new InscripcionEstadoResponse(idActividad, true, i.getIdInscripcion(), total))
                .orElse(new InscripcionEstadoResponse(idActividad, false, null, total));
        aplicarCupo(estado, idActividad);
        return estado;

    }

    // ====================================================================
    // Estado en lote
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public Map<Integer, InscripcionEstadoResponse> obtenerEstadoEnLote(
            List<Integer> idsActividades, Integer idActor, String tipoUsuario) {

        Map<Integer, InscripcionEstadoResponse> mapa = new HashMap<>();
        for (Integer id : idsActividades) {
            mapa.put(id, new InscripcionEstadoResponse(id, false, null, 0));
        }

        // Cargar conteo unificado para todas las actividades
        for (Integer id : idsActividades) {
            int total = conteoTotal(id);
            mapa.get(id).setTotalInscritos(total);
            aplicarCupo(mapa.get(id), id);
        }

        // Marcar las que el actor ya tiene
        if (idActor != null && tipoUsuario != null) {
            boolean esAlumno = "ALUMNO".equalsIgnoreCase(tipoUsuario);
            for (Integer id : idsActividades) {
                buscarInscripcionOpcional(id, idActor, esAlumno).ifPresent(i -> {
                    InscripcionEstadoResponse estado = mapa.get(id);
                    estado.setInscrito(true);
                    estado.setIdInscripcion(i.getIdInscripcion());
                });
            }
        }
        return mapa;
    }

    // ====================================================================
    // Total inscritos — SUMA usuarios del sistema + externos
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public Integer totalInscritos(Integer idActividad) {
        return conteoTotal(idActividad);
    }

    // ====================================================================
    // Helpers privados
    // ====================================================================

    /**
     * Suma inscritos del sistema (alumnos/usuarios) mas externos.
     * Es el unico punto donde se calcula el conteo; todos los metodos lo usan.
     */
    private int conteoTotal(Integer idActividad) {
        int inscritos = inscripcionRepository.countByActividad_IdActividad(idActividad);
        int externos  = externoRepository.countByActividad_IdActividad(idActividad);
        return inscritos + externos;
    }
    /**
     * US-29: obtiene la capacidad (aforo) del espacio asignado a la actividad.
     * Retorna null si la actividad no tiene un espacio asignado o el espacio
     * no define un limite de aforo valido (capacidad <= 0).
     */
    private Integer obtenerAforo(Integer idActividad) {
        List<ActividadRecurso> recursos = actividadRecursoRepository.findByActividadIdActividad(idActividad);
        if (recursos.isEmpty()) {
            return null;
        }

        List<Integer> idsRecursos = recursos.stream()
                .map(ar -> ar.getRecurso().getIdRecurso())
                .toList();

        List<RecursoEspacio> espacios = recursoEspacioRepository.findAllById(idsRecursos);
        if (espacios.isEmpty()) {
            return null;
        }

        Integer capacidad = espacios.get(0).getCapacidad();
        return (capacidad != null && capacidad > 0) ? capacidad : null;
    }
    /**
     * US-29: completa los campos de cupo (aforo, lugaresDisponibles, cupoLleno)
     * de un InscripcionEstadoResponse a partir del total de inscritos.
     */
    private void aplicarCupo(InscripcionEstadoResponse estado, Integer idActividad) {
        Integer aforo = obtenerAforo(idActividad);
        estado.setAforo(aforo);

        if (aforo == null) {
            estado.setLugaresDisponibles(null);
            estado.setCupoLleno(false);
            return;
        }

        int total = estado.getTotalInscritos() != null ? estado.getTotalInscritos() : 0;
        estado.setLugaresDisponibles(Math.max(aforo - total, 0));
        estado.setCupoLleno(total >= aforo);
    }

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

        LocalDateTime inicioEvento = actividad.getFechaActividad().atTime(actividad.getHoraInicio());
        if (LocalDateTime.now().isAfter(inicioEvento)) {
            throw new BusinessException("El periodo de inscripcion para esta actividad ha cerrado.");
        }
        return actividad;
    }

    private void verificarNoInscrito(Integer idActividad, Integer idActor, boolean esAlumno) {
        boolean yaInscrito = esAlumno
                ? inscripcionRepository
                .findByActividad_IdActividadAndAlumno_IdAlumno(idActividad, idActor)
                .isPresent()
                : inscripcionRepository
                .findByActividad_IdActividadAndUsuario_IdUsuario(idActividad, idActor)
                .isPresent();

        if (yaInscrito) {
            log.warn("Actor={} ya esta inscrito en actividad={}", idActor, idActividad);
            throw new BusinessException("Ya estas inscrito en esta actividad.");
        }
    }
    /**
     * US-29: bloquea la inscripcion si el cupo del evento ya esta lleno.
     */
    private void verificarCupoDisponible(Integer idActividad) {
        Integer aforo = obtenerAforo(idActividad);
        if (aforo == null) {
            return; // sin limite de aforo
        }
        int total = conteoTotal(idActividad);
        if (total >= aforo) {
            log.warn("Cupo lleno para actividad={} (aforo={}, inscritos={})", idActividad, aforo, total);
            throw new BusinessException("El cupo de esta actividad esta lleno.");
        }
    }

    private void verificarSinConflictoHorario(Actividad actividad, Integer idActor, boolean esAlumno) {
        List<InscripcionActividad> conflictos = esAlumno
                ? inscripcionRepository.findConflictosAlumno(
                idActor,
                actividad.getIdActividad(),
                actividad.getFechaActividad(),
                actividad.getHoraInicio(),
                actividad.getHoraFin())
                : inscripcionRepository.findConflictosUsuario(
                idActor,
                actividad.getIdActividad(),
                actividad.getFechaActividad(),
                actividad.getHoraInicio(),
                actividad.getHoraFin());

        if (!conflictos.isEmpty()) {
            String nombreConflicto = conflictos.get(0).getActividad().getNombre();
            log.warn("Conflicto de horario para actor={}: actividad conflictiva='{}'",
                    idActor, nombreConflicto);
            throw new BusinessException(
                    "Ya tienes inscripcion en '" + nombreConflicto +
                            "' que se solapa con este horario.");
        }
    }

    private InscripcionActividad buscarInscripcion(Integer idActividad, Integer idActor, boolean esAlumno) {
        return buscarInscripcionOpcional(idActividad, idActor, esAlumno)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro inscripcion activa para cancelar."));
    }

    private Optional<InscripcionActividad> buscarInscripcionOpcional(
            Integer idActividad, Integer idActor, boolean esAlumno) {
        return esAlumno
                ? inscripcionRepository.findByActividad_IdActividadAndAlumno_IdAlumno(idActividad, idActor)
                : inscripcionRepository.findByActividad_IdActividadAndUsuario_IdUsuario(idActividad, idActor);
    }

    private InscripcionResponse toResponse(InscripcionActividad i) {
        InscripcionResponse dto = new InscripcionResponse();
        dto.setIdInscripcion(i.getIdInscripcion());
        dto.setIdActividad(i.getActividad().getIdActividad());
        dto.setNombreActividad(i.getActividad().getNombre());
        dto.setCategoria(i.getActividad().getTipo().getCategoria().getNombre());
        dto.setTipo(i.getActividad().getTipo().getNombre());
        dto.setFechaActividad(i.getActividad().getFechaActividad());
        dto.setHoraInicio(i.getActividad().getHoraInicio());
        dto.setHoraFin(i.getActividad().getHoraFin());
        dto.setCampus(i.getActividad().getCampus() != null
                ? i.getActividad().getCampus().getNombre() : null);
        dto.setFechaInscripcion(i.getFechaInscripcion());

        imagenRepository.findByActividadIdActividadAndEsPortadaTrue(i.getActividad().getIdActividad())
                .ifPresent(img -> dto.setImagenPortada(img.getUrl()));

        return dto;
    }
}
