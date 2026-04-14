package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.SolicitudResponseDTO;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.service.ActividadService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor

public class ActividadServiceImpl implements ActividadService {
    private final ActividadRepository actividadRepository;

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
                .map(a -> new SolicitudResponseDTO(
                        a.getIdActividad(),
                        a.getNombre(),
                        a.getFechaActividad(),
                        a.getHoraInicio(),
                        a.getHoraFin(),
                        a.getEstado().name(),
                        a.getMotivoRechazo(),
                        a.getFechaRegistro()
                ))
                .toList();
    }
}
