package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.ActualizarActividadRequestDTO;
import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.exception.ActividadNoEditableException;
import mx.edu.unpa.actividadesapi.model.*;
import mx.edu.unpa.actividadesapi.service.storage.StorageService;
import mx.edu.unpa.actividadesapi.repository.*;
import mx.edu.unpa.actividadesapi.service.SolicitudesService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudesServiceImpl implements SolicitudesService {

    private final ActividadRepository actividadRepository;
    private final ActividadOrganizadorRepository organizadorRepository;
    private final ActividadRecursoRepository recursoRepository;
    private final ActividadImagenRepository imagenRepository;
    private final StorageService storageService;

    // ── US-04: Mis solicitudes ────────────────────────────────────────────────

    @Override
    public List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado) {
        List<Actividad> actividades;

        if (estado != null && !estado.isBlank()) {
            EstadoActividad estadoEnum = EstadoActividad.valueOf(estado.toUpperCase());
            actividades = actividadRepository
                    .findByProfesor_IdUsuarioAndEstadoOrderByFechaRegistroDesc(idProfesor, estadoEnum);
        } else {
            actividades = actividadRepository
                    .findByProfesor_IdUsuarioOrderByFechaRegistroDesc(idProfesor);
        }

        return actividades.stream()
                .map(this::toSolicitudResponseDTO)
                .toList();
    }

    // ── US-05: Editar actividad PENDIENTE ────────────────────────────────────

    @Override
    public SolicitudResponseDTO editarActividad(Integer idActividad, Integer idProfesor,
                                                ActualizarActividadRequestDTO dto) {

        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la actividad con ID: " + idActividad));

        if (!actividad.getProfesor().getIdUsuario().equals(idProfesor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para editar esta actividad");
        }

        if (actividad.getEstado() != EstadoActividad.PENDIENTE) {
            throw new ActividadNoEditableException(
                    "Solo se pueden editar actividades en estado PENDIENTE. " +
                            "Estado actual: " + actividad.getEstado());
        }

        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());
        actividad.setFechaActividad(dto.getFechaActividad());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());

        if (dto.getIdTipo() != null) {
            actividad.setTipo(dto.getIdTipo());
        }

        Actividad actividadActualizada = actividadRepository.save(actividad);
        return toSolicitudResponseDTO(actividadActualizada);
    }

    // ── Imagen: reemplazar (o agregar si no había) ────────────────────────────
    // Regla de negocio: solo UNA imagen por actividad (la portada).
    // Si ya existe una imagen se borra del storage y de la BD antes de guardar la nueva.

    @Override
    @Transactional
    public SolicitudResponseDTO reemplazarImagen(Integer idActividad, Integer idProfesor,
                                                 MultipartFile imagen) {

        Actividad actividad = obtenerActividadPendientePropia(idActividad, idProfesor);

        // Eliminar imagen existente si la hay
        eliminarImagenExistente(idActividad);

        // Guardar nueva imagen como portada
        String url = storageService.guardar(imagen, "actividades/" + idActividad);

        ActividadImagen img = new ActividadImagen();
        img.setActividad(actividad);
        img.setUrl(url);
        img.setNombreArchivo(imagen.getOriginalFilename());
        img.setEsPortada(true);   // siempre portada, es la única imagen
        imagenRepository.save(img);

        return toSolicitudResponseDTO(actividad);
    }

    // ── Imagen: eliminar sin reemplazar ───────────────────────────────────────

    @Override
    @Transactional
    public SolicitudResponseDTO eliminarImagen(Integer idActividad, Integer idProfesor) {

        Actividad actividad = obtenerActividadPendientePropia(idActividad, idProfesor);
        eliminarImagenExistente(idActividad);
        return toSolicitudResponseDTO(actividad);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private Actividad obtenerActividadPendientePropia(Integer idActividad, Integer idProfesor) {
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la actividad con ID: " + idActividad));

        if (!actividad.getProfesor().getIdUsuario().equals(idProfesor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar esta actividad");
        }

        if (actividad.getEstado() != EstadoActividad.PENDIENTE) {
            throw new ActividadNoEditableException(
                    "Solo se pueden modificar imágenes de actividades en estado PENDIENTE.");
        }

        return actividad;
    }

    /** Borra del storage y de la BD la imagen actual (si existe). */
    private void eliminarImagenExistente(Integer idActividad) {
        List<ActividadImagen> imagenes = imagenRepository.findByActividadIdActividad(idActividad);
        for (ActividadImagen img : imagenes) {
            try {
                storageService.eliminar(img.getUrl());
            } catch (Exception e) {
                // Si falla el borrado físico no bloqueamos la operación
            }
            imagenRepository.delete(img);
        }
    }

    // ── Mapper entidad → DTO ─────────────────────────────────────────────────

    private SolicitudResponseDTO toSolicitudResponseDTO(Actividad a) {

        List<String> organizadores = organizadorRepository
                .findByActividadIdActividad(a.getIdActividad())
                .stream()
                .map(org -> {
                    if (org.getCarrera() != null) return org.getCarrera().getNombre();
                    if (org.getDepartamento() != null) return org.getDepartamento().getNombre();
                    return "";
                })
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());

        List<SolicitudResponseDTO.RecursoResumenDTO> recursos = recursoRepository
                .findByActividadIdActividad(a.getIdActividad())
                .stream()
                .map(ar -> new SolicitudResponseDTO.RecursoResumenDTO(
                        ar.getRecurso().getIdRecurso(),
                        ar.getRecurso().getNombre(),
                        ar.getRecurso().getTipoRecurso().getNombre(),
                        ar.getCantidadRequerida()
                ))
                .collect(Collectors.toList());

        List<SolicitudResponseDTO.ImagenDTO> imagenes = imagenRepository
                .findByActividadIdActividad(a.getIdActividad())
                .stream()
                .map(img -> new SolicitudResponseDTO.ImagenDTO(
                        img.getIdImagen(),
                        img.getUrl(),
                        img.getNombreArchivo(),
                        img.getEsPortada(),
                        img.getFechaSubida()
                ))
                .collect(Collectors.toList());

        return new SolicitudResponseDTO(
                a.getIdActividad(),
                a.getNombre(),
                a.getCampus().getNombre(),
                a.getDescripcion(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getEstado(),
                a.getMotivoRechazo(),
                a.getFechaRegistro(),
                a.getFechaActualizacion(),
                a.getRequiereInscripcion(),   // US-31
                a.getTipo().getNombre(),
                a.getTipo().getCategoria() != null ? a.getTipo().getCategoria().getNombre() : null,
                organizadores,
                recursos,
                imagenes,
                a.getRequiereInscripcion()
        );
    }
}