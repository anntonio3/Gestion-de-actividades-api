package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import java.util.List;

public interface SolicitudesService {
    // Interface antes llamada ActividadService

    List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado);

}