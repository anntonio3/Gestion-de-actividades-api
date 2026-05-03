package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.service.SolicitudesService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor

public class SolicitudesServiceImpl implements SolicitudesService {
    // Clase antes llamada ActividadServiceImpl

    private final ActividadRepository actividadRepository;

    public List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado) {
        List<Actividad> actividades;

        if (estado != null && !estado.isBlank()) {
            EstadoActividad estadoEnum = EstadoActividad.valueOf(estado.toUpperCase());
            actividades = actividadRepository
                    .findByIdProfesorAndEstadoOrderByFechaRegistroDesc(idProfesor, estadoEnum);
        } else {
            actividades = actividadRepository
                    .findByIdProfesorOrderByFechaRegistroDesc(idProfesor);
        }

        return actividades.stream()
                .map(a -> new SolicitudResponseDTO(
                        a.getIdActividad(),
                        a.getNombre(),
                        a.getFechaActividad(),
                        a.getHoraInicio(),
                        a.getHoraFin(),
                        a.getEstado(),
                        a.getMotivoRechazo(),
                        a.getFechaRegistro()
                ))
                .toList();
    }
}
