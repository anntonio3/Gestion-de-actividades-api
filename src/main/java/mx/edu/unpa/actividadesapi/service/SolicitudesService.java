package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.ActualizarActividadRequestDTO;
import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import java.util.List;

public interface SolicitudesService {
    // Interface antes llamada ActividadService

    List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado);

    // US-05: Editar una actividad en estado Pendiente
    SolicitudResponseDTO editarActividad(Integer idActividad, Integer idProfesor,
                                          ActualizarActividadRequestDTO dto);
}