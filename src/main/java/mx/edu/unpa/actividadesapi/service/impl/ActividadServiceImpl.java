package mx.edu.unpa.actividadesapi.service.impl;

import mx.edu.unpa.actividadesapi.dto.request.ActividadRequest;
import mx.edu.unpa.actividadesapi.dto.request.ActividadRecursoRequest;
import mx.edu.unpa.actividadesapi.dto.response.ActividadResponse;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.ActividadService;
import mx.edu.unpa.actividadesapi.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActividadServiceImpl implements ActividadService {

    private static final Logger log = LoggerFactory.getLogger(ActividadServiceImpl.class);

    @Autowired
    private ActividadRepository actividadRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TipoActividadRepository tipoActividadRepository;
    @Autowired
    private RecursoRepository recursoRepository;
    @Autowired
    private ActividadRecursoRepository actividadRecursoRepository;

    // Nuevas inyecciones en el constructor
    @Autowired
    private CarreraRepository carreraRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private ActividadOrganizadorRepository organizadorRepository;
    @Autowired
    private ActividadImagenRepository imagenRepository;
    @Autowired
    private StorageService storageService;

    @Autowired
    private RecursoEspacioRepository recursoEspacioRepository;
    @Autowired
    private RecursoMobiliarioRepository recursoMobiliarioRepository;


    @Override
    @Transactional
    public ActividadResponse registrarActividad(ActividadRequest request, MultipartFile portada) {
        log.info("Iniciando registro de actividad para profesor id={}", request.getIdProfesor());

        // 1. Validar que hora_fin > hora_inicio
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // 2. Validar que el profesor existe y tiene rol PROFESOR
        Usuario profesor = usuarioRepository.findById(request.getIdProfesor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor no encontrado con id: " + request.getIdProfesor()));

        if (profesor.getRol() != Rol.PROFESOR) {
            throw new BusinessException("El usuario con id " + request.getIdProfesor() + " no tiene rol PROFESOR");
        }

        if (!profesor.getActivo()) {
            throw new BusinessException("El profesor con id " + request.getIdProfesor() + " no está activo");
        }

        // 3. Validar tipo de actividad
        TipoActividad tipo = tipoActividadRepository.findById(request.getIdTipo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de actividad no encontrado con id: " + request.getIdTipo()));

        // 4. Crear y guardar la actividad
        Actividad actividad = new Actividad();
        actividad.setProfesor(profesor);
        actividad.setTipo(tipo);
        actividad.setNombre(request.getNombre());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setFechaActividad(request.getFechaActividad());
        actividad.setHoraInicio(request.getHoraInicio());
        actividad.setHoraFin(request.getHoraFin());
        actividad.setEstado(EstadoActividad.PENDIENTE);

        Actividad guardada = actividadRepository.save(actividad);
        log.info("Actividad creada con id={}, estado=PENDIENTE", guardada.getIdActividad());

        // 5. Validar y guardar los recursos solicitados
        List<ActividadRecurso> recursosGuardados = new ArrayList<>();

        for (ActividadRecursoRequest recursoReq : request.getRecursos()) {
            Recurso recurso = recursoRepository.findById(recursoReq.getIdRecurso())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Recurso no encontrado con id: " + recursoReq.getIdRecurso()));


            // Si es espacio, verificar que esté libre
            if (recurso instanceof RecursoEspacio) {
                var ocupados = recursoEspacioRepository.findIdsOcupados(
                        request.getFechaActividad(),
                        request.getHoraInicio(),
                        request.getHoraFin());
                if (ocupados.contains(recurso.getIdRecurso())) {
                    throw new BusinessException(
                            "El espacio '" + recurso.getNombre() + "' ya está ocupado en ese horario");
                }
            }

            // Si es mobiliario, verificar cantidad disponible
            if (recurso instanceof RecursoMobiliario mobiliario) {
                var ocupadas = recursoMobiliarioRepository.findCantidadesOcupadas(
                        request.getFechaActividad(),
                        request.getHoraInicio(),
                        request.getHoraFin());

                int ocupada = ocupadas.stream()
                        .filter(row -> ((Long) row[0]).equals(recurso.getIdRecurso()))
                        .mapToInt(row -> ((Number) row[1]).intValue())
                        .findFirst().orElse(0);

                int disponible = mobiliario.getExistencias() - ocupada;
                if (recursoReq.getCantidadRequerida() > disponible) {
                    throw new BusinessException(
                            "Solo hay " + disponible + " unidades disponibles de '"
                                    + recurso.getNombre() + "' en ese horario");
                }
            }

            if (!recurso.getActivo()) {
                throw new BusinessException("El recurso '" + recurso.getNombre() + "' no está disponible");
            }

            ActividadRecurso ar = new ActividadRecurso();
            ar.setActividad(guardada);
            ar.setRecurso(recurso);
            ar.setCantidadRequerida(recursoReq.getCantidadRequerida());
            recursosGuardados.add(actividadRecursoRepository.save(ar));

            log.info("Recurso id={} asignado a actividad id={}, cantidad={}",
                    recurso.getIdRecurso(), guardada.getIdActividad(), recursoReq.getCantidadRequerida());
        }

        log.info("Actividad id={} registrada exitosamente con {} recurso(s)",
                guardada.getIdActividad(), recursosGuardados.size());


        // NUEVO : Guardar organizadores
        for (ActividadRequest.OrganizadorRequest org : request.getOrganizadores()) {
            if (org.getIdCarrera() == null && org.getIdDepartamento() == null) {
                throw new BusinessException("Cada organizador debe tener al menos carrera o departamento");
            }
            ActividadOrganizador organizador = new ActividadOrganizador();
            organizador.setActividad(guardada);

            if (org.getIdCarrera() != null) {
                Carrera carrera = carreraRepository.findById(org.getIdCarrera())
                        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada: " + org.getIdCarrera()));
                organizador.setCarrera(carrera);
            }
            if (org.getIdDepartamento() != null) {
                Departamento depto = departamentoRepository.findById(org.getIdDepartamento())
                        .orElseThrow(() -> new ResourceNotFoundException("Departamento no encontrado: " + org.getIdDepartamento()));
                organizador.setDepartamento(depto);
            }
            organizadorRepository.save(organizador);
        }



        // Guardar imagen de portada (opcional)
        if (portada != null && !portada.isEmpty()) {
            String url = storageService.guardar(portada, "actividad-" + guardada.getIdActividad());
            ActividadImagen imagen = new ActividadImagen();
            imagen.setActividad(guardada);
            imagen.setUrl(url);
            imagen.setNombreArchivo(portada.getOriginalFilename());
            imagen.setEsPortada(true);
            imagenRepository.save(imagen);
            log.info("Portada guardada para actividad id={}", guardada.getIdActividad());
        }

        return toResponse(guardada, recursosGuardados);
    }

    // Mapeo entidad → DTO de respuesta
    private ActividadResponse toResponse(Actividad actividad, List<ActividadRecurso> recursos) {
        ActividadResponse response = new ActividadResponse();
        response.setIdActividad(actividad.getIdActividad());
        response.setNombreProfesor(actividad.getProfesor().getNombre()
                + " " + actividad.getProfesor().getApellidos());
        response.setTipoActividad(actividad.getTipo().getNombre());
        response.setCategoria(actividad.getTipo().getCategoria().getNombre());
        response.setNombre(actividad.getNombre());
        response.setDescripcion(actividad.getDescripcion());
        response.setFechaActividad(actividad.getFechaActividad());
        response.setHoraInicio(actividad.getHoraInicio());
        response.setHoraFin(actividad.getHoraFin());
        response.setEstado(actividad.getEstado());
        response.setFechaRegistro(actividad.getFechaRegistro());

        List<ActividadResponse.RecursoResumen> resumenes = recursos.stream().map(ar -> {
            ActividadResponse.RecursoResumen r = new ActividadResponse.RecursoResumen();
            r.setIdRecurso(ar.getRecurso().getIdRecurso());
            r.setNombre(ar.getRecurso().getNombre());
            r.setTipoRecurso(ar.getRecurso().getTipoRecurso().getNombre());
            r.setCantidadRequerida(ar.getCantidadRequerida());
            return r;
        }).toList();

        response.setRecursos(resumenes);
        return response;
    }
}