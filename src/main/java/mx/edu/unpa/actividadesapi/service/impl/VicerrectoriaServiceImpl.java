package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.request.AprobarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.request.DestacarRequest;
import mx.edu.unpa.actividadesapi.dto.request.RechazarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.response.DestacarResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDecididaResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudListItemResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.enums.NivelImportancia;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.DestacadoConflictoException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.VicerrectoriaService;
import mx.edu.unpa.actividadesapi.repository.RecursoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class VicerrectoriaServiceImpl implements VicerrectoriaService {

    private static final Logger log = LoggerFactory.getLogger(VicerrectoriaServiceImpl.class);

    @Autowired private ActividadRepository actividadRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ActividadRecursoRepository actividadRecursoRepository;
    @Autowired private ActividadOrganizadorRepository organizadorRepository;
    @Autowired private ActividadImagenRepository imagenRepository;
    @Autowired private RecursoRepository recursoRepository;


    // ====================================================================
    // US-07: Listar solicitudes
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public List<SolicitudListItemResponse> listarSolicitudes(String estado,
                                                             Integer idCategoria,
                                                             Integer idCarrera,
                                                             Integer idDepartamento,
                                                             String busqueda) {
        log.info("US-07: listar solicitudes filtros[estado={}, cat={}, carr={}, dept={}, q={}]",
                estado, idCategoria, idCarrera, idDepartamento, busqueda);

        // Para mantenerlo simple: traer todas y filtrar en memoria.
        // Es viable mientras el volumen de actividades sea bajo (caso academico).
        // Si crece, se migra a Specifications o JPQL dinamico.
        List<Actividad> todas = actividadRepository.findAllByOrderByFechaRegistroDesc();

        EstadoActividad estadoEnum = parseEstado(estado);
        String q = (busqueda == null) ? "" : busqueda.trim().toLowerCase();

        return todas.stream()
                .filter(a -> estadoEnum == null || a.getEstado() == estadoEnum)
                .filter(a -> idCategoria == null
                        || (a.getTipo() != null
                        && a.getTipo().getCategoria() != null
                        && idCategoria.equals(a.getTipo().getCategoria().getIdCategoria())))
                .filter(a -> coincideOrganizador(a, idCarrera, idDepartamento))
                .filter(a -> coincideBusqueda(a, q))
                .map(this::toListItem)
                .toList();
    }

    // ====================================================================
    // US-10: Detalle completo + conflictos
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public SolicitudDetalleResponse obtenerDetalle(Integer idActividad) {
        log.info("US-10: obteniendo detalle de actividad id={}", idActividad);
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));
        return toDetalle(actividad);
    }

    // ====================================================================
    // US-08: Aprobar
    // ====================================================================
    @Override
    @Transactional
    public SolicitudDecididaResponse aprobar(Integer idActividad, AprobarActividadRequest request) {
        log.info("US-08: aprobando actividad id={} por admin id={}", idActividad, request.getIdAdmin());

        Actividad actividad = obtenerActividadPendiente(idActividad);
        Usuario admin = obtenerAdminValido(request.getIdAdmin());

        actividad.setEstado(EstadoActividad.APROBADA);
        actividad.setVicerrector(admin);
        actividad.setFechaRevision(LocalDateTime.now());
        // El comentario al aprobar es opcional; si viene, se guarda en motivoRechazo
        // como nota de revision (la columna se reusa para no agregar otra).
        // Si prefieres separar comentario_aprobacion vs motivo_rechazo, dime y lo agrego.
        if (request.getComentario() != null && !request.getComentario().isBlank()) {
            actividad.setMotivoRechazo(request.getComentario());
        } else {
            actividad.setMotivoRechazo(null);
        }

        Actividad guardada = actividadRepository.save(actividad);
        log.info("Actividad id={} APROBADA por admin id={}", idActividad, admin.getIdUsuario());

        SolicitudDecididaResponse dto = toDecidida(guardada);
        // US-25: sugerir destacado solo si el tipo es DESTACADO.
        // El front decide si abre el modal; el reemplazo se confirma aparte.
        boolean tipoDestacado = guardada.getTipo() != null
                && guardada.getTipo().getNivelImportancia() == NivelImportancia.DESTACADO;
        dto.setSugerirDestacado(tipoDestacado);
        if (tipoDestacado) {
            log.info("US-25: actividad id={} es de tipo DESTACADO, se sugiere destacar", idActividad);
        }
        return dto;
    }

    // ====================================================================
    // US-09: Rechazar
    // ====================================================================
    @Override
    @Transactional
    public SolicitudDecididaResponse rechazar(Integer idActividad, RechazarActividadRequest request) {
        log.info("US-09: rechazando actividad id={} por admin id={}", idActividad, request.getIdAdmin());

        Actividad actividad = obtenerActividadPendiente(idActividad);
        Usuario admin = obtenerAdminValido(request.getIdAdmin());

        actividad.setEstado(EstadoActividad.RECHAZADA);
        actividad.setVicerrector(admin);
        actividad.setFechaRevision(LocalDateTime.now());
        actividad.setMotivoRechazo(request.getMotivo().trim());

        Actividad guardada = actividadRepository.save(actividad);
        log.info("Actividad id={} RECHAZADA por admin id={}", idActividad, admin.getIdUsuario());

        return toDecidida(guardada);
    }

    // ====================================================================
    // Helpers de validacion
    // ====================================================================

    /**
     * Recupera la actividad y valida que este en estado PENDIENTE.
     * No se puede aprobar/rechazar una actividad ya decidida.
     */
    private Actividad obtenerActividadPendiente(Integer idActividad) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        if (actividad.getEstado() != EstadoActividad.PENDIENTE) {
            throw new BusinessException(
                    "Solo se pueden revisar actividades en estado PENDIENTE. " +
                            "Estado actual: " + actividad.getEstado());
        }
        return actividad;
    }

    /**
     * Verifica que el usuario indicado exista, tenga rol ADMIN y este activo.
     */
    private Usuario obtenerAdminValido(Integer idAdmin) {
        Usuario admin = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Administrador no encontrado con id: " + idAdmin));
        if (admin.getRol() != Rol.ADMIN) {
            throw new BusinessException(
                    "El usuario id=" + idAdmin + " no tiene rol ADMIN");
        }
        if (Boolean.FALSE.equals(admin.getActivo())) {
            throw new BusinessException(
                    "El administrador id=" + idAdmin + " no esta activo");
        }
        return admin;
    }

    private EstadoActividad parseEstado(String estado) {
        if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
            return null;
        }
        try {
            return EstadoActividad.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Estado invalido: " + estado);
        }
    }

    private boolean coincideOrganizador(Actividad a, Integer idCarrera, Integer idDepartamento) {
        if (idCarrera == null && idDepartamento == null) return true;
        List<ActividadOrganizador> orgs = organizadorRepository
                .findByActividadIdActividad(a.getIdActividad());
        return orgs.stream().anyMatch(o ->
                (idCarrera != null && o.getCarrera() != null
                        && idCarrera.equals(o.getCarrera().getIdCarrera()))
                        || (idDepartamento != null && o.getDepartamento() != null
                        && idDepartamento.equals(o.getDepartamento().getIdDepartamento()))
        );
    }

    private boolean coincideBusqueda(Actividad a, String q) {
        if (q.isEmpty()) return true;
        String nombre = a.getNombre() == null ? "" : a.getNombre().toLowerCase();
        String prof = (a.getProfesor() == null ? "" :
                (a.getProfesor().getNombre() + " " + a.getProfesor().getApellidos())).toLowerCase();
        return nombre.contains(q) || prof.contains(q);
    }


    // ====================================================================
    // US-26: Destacar / quitar destacado
    // ====================================================================

    @Override
    @Transactional
    public DestacarResponse destacar(Integer idActividad, DestacarRequest request) {
        log.info("US-26: destacar actividad id={} por admin id={} confirmarReemplazo={}",
                idActividad, request.getIdAdmin(), request.getConfirmarReemplazo());

        Usuario admin = obtenerAdminValido(request.getIdAdmin());

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        // Solo se destacan actividades aprobadas
        if (actividad.getEstado() != EstadoActividad.APROBADA) {
            throw new BusinessException(
                    "Solo se pueden destacar actividades aprobadas. Estado actual: "
                            + actividad.getEstado());
        }

        // Si ya esta destacada, no hay nada que hacer
        if (Boolean.TRUE.equals(actividad.getDestacadoActivo())) {
            log.info("Actividad id={} ya estaba destacada", idActividad);
            return toDestacarResponse(actividad);
        }

        // Verificar si hay otro destacado activo
        Optional<Actividad> destacadoActual = actividadRepository.findByDestacadoActivoTrue();

        if (destacadoActual.isPresent()) {
            Actividad actual = destacadoActual.get();
            // El front debe confirmar el reemplazo (decision de diseno del ticket)
            if (!Boolean.TRUE.equals(request.getConfirmarReemplazo())) {
                log.warn("US-26: ya existe destacado activo id={} y no se confirmo reemplazo",
                        actual.getIdActividad());
                throw new DestacadoConflictoException(
                        actual.getIdActividad(), actual.getNombre());
            }
            // Reemplazo confirmado: desactivar el anterior
            actual.setDestacadoActivo(false);
            actual.setDestacadoPor(null);
            actual.setFechaDestacado(null);
            actividadRepository.save(actual);
            // Flush para liberar el indice unico antes de activar el nuevo
            actividadRepository.flush();
            log.info("US-26: destacado anterior id={} desactivado", actual.getIdActividad());
        }

        actividad.setDestacadoActivo(true);
        actividad.setDestacadoPor(admin);
        actividad.setFechaDestacado(LocalDateTime.now());
        Actividad guardada = actividadRepository.save(actividad);

        log.info("US-26: actividad id={} marcada como destacada por admin id={}",
                idActividad, admin.getIdUsuario());
        return toDestacarResponse(guardada);
    }

    @Override
    @Transactional
    public void quitarDestacado(Integer idActividad, Integer idAdmin) {
        log.info("US-26: quitar destacado de actividad id={} por admin id={}",
                idActividad, idAdmin);

        obtenerAdminValido(idAdmin);

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Actividad no encontrada con id: " + idActividad));

        if (!Boolean.TRUE.equals(actividad.getDestacadoActivo())) {
            log.info("Actividad id={} no estaba destacada, nada que quitar", idActividad);
            return;
        }

        actividad.setDestacadoActivo(false);
        actividad.setDestacadoPor(null);
        actividad.setFechaDestacado(null);
        actividadRepository.save(actividad);
        log.info("US-26: destacado quitado de actividad id={}", idActividad);
    }

    private DestacarResponse toDestacarResponse(Actividad a) {
        DestacarResponse dto = new DestacarResponse();
        dto.setIdActividad(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDestacadoActivo(a.getDestacadoActivo());
        dto.setNombreAdmin(nombreCompleto(a.getDestacadoPor()));
        dto.setFechaDestacado(a.getFechaDestacado());
        return dto;
    }

    // ====================================================================
    // Mapeos a DTOs
    // ====================================================================

    private SolicitudListItemResponse toListItem(Actividad a) {
        SolicitudListItemResponse dto = new SolicitudListItemResponse();
        dto.setIdActividad(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setNombreProfesor(nombreCompleto(a.getProfesor()));
        dto.setCorreoProfesor(a.getProfesor() != null ? a.getProfesor().getCorreo() : null);
        dto.setTipoActividad(a.getTipo() != null ? a.getTipo().getNombre() : null);
        dto.setCampus(a.getCampus() != null ? a.getCampus().getNombre() : null);
        dto.setCategoria(a.getTipo() != null && a.getTipo().getCategoria() != null
                ? a.getTipo().getCategoria().getNombre() : null);
        dto.setFechaActividad(a.getFechaActividad());
        dto.setHoraInicio(a.getHoraInicio());
        dto.setHoraFin(a.getHoraFin());
        dto.setEstado(a.getEstado());
        dto.setFechaRegistro(a.getFechaRegistro());

        List<ActividadOrganizador> orgs = organizadorRepository
                .findByActividadIdActividad(a.getIdActividad());
        dto.setOrganizadores(orgs.stream().map(this::nombreOrganizador).toList());

        List<ActividadRecurso> recursos = actividadRecursoRepository
                .findByActividadIdActividad(a.getIdActividad());
        dto.setTotalRecursos(recursos.size());

        return dto;
    }

    private SolicitudDetalleResponse toDetalle(Actividad a) {
        SolicitudDetalleResponse dto = new SolicitudDetalleResponse();
        dto.setIdActividad(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setNombreProfesor(nombreCompleto(a.getProfesor()));
        dto.setCorreoProfesor(a.getProfesor() != null ? a.getProfesor().getCorreo() : null);
        dto.setTipoActividad(a.getTipo() != null ? a.getTipo().getNombre() : null);
        dto.setCampus(a.getCampus() != null ? a.getCampus().getNombre() : null);
        dto.setCategoria(a.getTipo() != null && a.getTipo().getCategoria() != null
                ? a.getTipo().getCategoria().getNombre() : null);
        dto.setFechaActividad(a.getFechaActividad());
        dto.setHoraInicio(a.getHoraInicio());
        dto.setHoraFin(a.getHoraFin());
        dto.setEstado(a.getEstado());
        dto.setFechaRegistro(a.getFechaRegistro());
        dto.setMotivoRechazo(a.getMotivoRechazo());
        dto.setFechaRevision(a.getFechaRevision());
        dto.setNombreVicerrector(nombreCompleto(a.getVicerrector()));
        dto.setVersion(a.getVersion());

        // Portada
        imagenRepository.findByActividadIdActividadAndEsPortadaTrue(a.getIdActividad())
                .ifPresent(img -> dto.setUrlPortada(img.getUrl()));

        // Organizadores
        List<ActividadOrganizador> orgs = organizadorRepository
                .findByActividadIdActividad(a.getIdActividad());
        dto.setOrganizadores(orgs.stream().map(this::nombreOrganizador).toList());

        // Recursos detallados
        List<ActividadRecurso> recursos = actividadRecursoRepository
                .findByActividadIdActividad(a.getIdActividad());
        dto.setRecursos(recursos.stream().map(this::toRecursoDetalle).toList());

        // Conflictos: solo se calculan para actividades aun pendientes,
        // ya tomada la decision pierde sentido mostrarlos.
        if (a.getEstado() == EstadoActividad.PENDIENTE) {
            dto.setConflictos(detectarConflictos(a, recursos));
        } else {
            dto.setConflictos(List.of());
        }

        return dto;
    }

    private SolicitudDetalleResponse.RecursoDetalle toRecursoDetalle(ActividadRecurso ar) {
        SolicitudDetalleResponse.RecursoDetalle d = new SolicitudDetalleResponse.RecursoDetalle();
        Recurso r = ar.getRecurso();

        d.setIdRecurso(r.getIdRecurso());
        d.setNombre(r.getNombre());
        d.setTipoRecurso(r.getTipoRecurso() != null ? r.getTipoRecurso().getNombre() : null);
        d.setCantidadRequerida(ar.getCantidadRequerida());

        if (r instanceof RecursoEspacio espacio) {
            d.setCapacidad(espacio.getCapacidad());
            d.setUbicacion(espacio.getUbicacion());
        } else if (r instanceof RecursoMobiliario mob) {
            d.setCantidadInventario(mob.getExistencias());
        }
        return d;
    }

    /**
     * US-10: Detecta conflictos contra otras actividades APROBADAS
     * en el mismo dia y rango horario.
     */
    private List<SolicitudDetalleResponse.ConflictoRecurso> detectarConflictos(
            Actividad a, List<ActividadRecurso> recursos) {

        List<SolicitudDetalleResponse.ConflictoRecurso> conflictos = new ArrayList<>();

        // Espacios ocupados en ese rango (excluyendo esta misma actividad)
        List<Integer> espaciosOcupados = actividadRepository.findEspaciosOcupadosExcluyendo(
                a.getIdActividad(), a.getFechaActividad(), a.getHoraInicio(), a.getHoraFin());

        // Mobiliario ocupado (mapa idRecurso -> cantidad)
        // OJO: castea a Number para evitar el bug Long vs Integer (existente en US-03).
        Map<Integer, Integer> mobiliarioOcupado = new HashMap<>();
        List<Object[]> rowsMob = actividadRepository.findMobiliarioOcupadoExcluyendo(
                a.getIdActividad(), a.getFechaActividad(), a.getHoraInicio(), a.getHoraFin());
        for (Object[] row : rowsMob) {
            Integer idRec = ((Number) row[0]).intValue();
            Integer cant = ((Number) row[1]).intValue();
            mobiliarioOcupado.put(idRec, cant);
        }

        for (ActividadRecurso ar : recursos) {
            Recurso r = ar.getRecurso();

            if (r instanceof RecursoEspacio) {
                if (espaciosOcupados.contains(r.getIdRecurso())) {
                    SolicitudDetalleResponse.ConflictoRecurso c =
                            new SolicitudDetalleResponse.ConflictoRecurso();
                    c.setIdRecurso(r.getIdRecurso());
                    c.setNombreRecurso(r.getNombre());
                    c.setTipoRecurso("ESPACIO");
                    c.setMensaje("El espacio ya esta reservado por otra actividad aprobada en ese horario.");
                    c.setCantidadSolicitada(1);
                    c.setCantidadDisponible(0);
                    conflictos.add(c);
                }
            } else if (r instanceof RecursoMobiliario mob) {
                int ocupado = mobiliarioOcupado.getOrDefault(r.getIdRecurso(), 0);
                int disponible = Math.max(0, mob.getExistencias() - ocupado);
                if (ar.getCantidadRequerida() > disponible) {
                    SolicitudDetalleResponse.ConflictoRecurso c =
                            new SolicitudDetalleResponse.ConflictoRecurso();
                    c.setIdRecurso(r.getIdRecurso());
                    c.setNombreRecurso(r.getNombre());
                    c.setTipoRecurso("MOBILIARIO");
                    c.setMensaje("Solicita " + ar.getCantidadRequerida()
                            + " unidades pero solo hay " + disponible + " disponibles en ese horario.");
                    c.setCantidadSolicitada(ar.getCantidadRequerida());
                    c.setCantidadDisponible(disponible);
                    conflictos.add(c);
                }
            }
        }
        return conflictos;
    }

    private SolicitudDecididaResponse toDecidida(Actividad a) {
        SolicitudDecididaResponse dto = new SolicitudDecididaResponse();
        dto.setIdActividad(a.getIdActividad());
        dto.setEstado(a.getEstado());
        dto.setMotivoRechazo(a.getMotivoRechazo());
        dto.setFechaRevision(a.getFechaRevision());
        dto.setNombreVicerrector(nombreCompleto(a.getVicerrector()));
        dto.setVersion(a.getVersion());
        return dto;
    }

    private String nombreCompleto(Usuario u) {
        if (u == null) return null;
        return (u.getNombre() == null ? "" : u.getNombre()) + " " +
                (u.getApellidos() == null ? "" : u.getApellidos());
    }

    private String nombreOrganizador(ActividadOrganizador o) {
        if (o.getCarrera() != null) return o.getCarrera().getNombre();
        if (o.getDepartamento() != null) return o.getDepartamento().getNombre();
        return "Sin especificar";
    }
}
