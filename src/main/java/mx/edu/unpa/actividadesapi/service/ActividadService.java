package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.SolicitudResponseDTO;
import java.util.List;

public interface ActividadService {


    List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado);

}