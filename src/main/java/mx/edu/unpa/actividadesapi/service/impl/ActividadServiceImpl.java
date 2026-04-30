package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.ActualizarActividadRequestDTO;
import mx.edu.unpa.actividadesapi.dto.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.exception.ActividadNoEditableException;
import org.springframework.http.HttpStatus;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.service.ActividadService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor

public class ActividadServiceImpl implements ActividadService {
    private final ActividadRepository actividadRepository;

    @Override
    public List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado) {
        List<Actividad> actividades;

        if (estado != null && !estado.isBlank()) {
            Actividad.EstadoActividad estadoEnum = Actividad.EstadoActividad.valueOf(estado.toUpperCase());
            actividades = actividadRepository
                    .findByIdProfesorAndEstadoOrderByFechaRegistroDesc(idProfesor, estadoEnum);
        } else {
            actividades = actividadRepository
                    .findByIdProfesorOrderByFechaRegistroDesc(idProfesor);
        }

        return actividades.stream()
                .map(this::toSolicitudResponseDTO)
                .toList();
    }

    // US-05
    @Override
    public SolicitudResponseDTO editarActividad(Integer idActividad, Integer idProfesor,
                                                ActualizarActividadRequestDTO dto) {

        // Verificar que la actividad existe
        Actividad actividad = actividadRepository.findById(idActividad)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la actividad con ID: " + idActividad));

        // Verificar que la actividad pertenece al profesor
        if (!actividad.getIdProfesor().equals(idProfesor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para editar esta actividad");
        }

        // Verificar que la actividad está en estado PENDIENTE
        if (actividad.getEstado() != Actividad.EstadoActividad.PENDIENTE) {
            throw new ActividadNoEditableException(
                    "Solo se pueden editar actividades en estado PENDIENTE. " +
                            "Estado actual: " + actividad.getEstado());
        }

        // Aplicar cambios
        actividad.setNombre(dto.getNombre());
        actividad.setDescripcion(dto.getDescripcion());
        actividad.setFechaActividad(dto.getFechaActividad());
        actividad.setHoraInicio(dto.getHoraInicio());
        actividad.setHoraFin(dto.getHoraFin());

        if (dto.getIdTipo() != null) {
            actividad.setIdTipo(dto.getIdTipo());
        }

        // Registrar fecha de última modificación
        actividad.setFechaActualizacion(LocalDateTime.now());

        Actividad actividadActualizada = actividadRepository.save(actividad);
        return toSolicitudResponseDTO(actividadActualizada);
    }

    // Metodo auxiliar para mapear entidad
    private SolicitudResponseDTO toSolicitudResponseDTO(Actividad a) {
        return new SolicitudResponseDTO(
                a.getIdActividad(),
                a.getNombre(),
                a.getDescripcion(),
                a.getFechaActividad(),
                a.getHoraInicio(),
                a.getHoraFin(),
                a.getEstado().name(),
                a.getMotivoRechazo(),
                a.getFechaRegistro()
        );
    }
}
