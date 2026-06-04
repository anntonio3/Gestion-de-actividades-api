package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.ActualizarActividadRequestDTO;
import mx.edu.unpa.actividadesapi.dto.response.SolicitudResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SolicitudesService {

    List<SolicitudResponseDTO> getMisSolicitudes(Integer idProfesor, String estado);

    SolicitudResponseDTO editarActividad(Integer idActividad, Integer idProfesor,
                                         ActualizarActividadRequestDTO dto);

    /** Reemplaza (o agrega si no había) la portada de la actividad. Solo una imagen permitida. */
    SolicitudResponseDTO reemplazarImagen(Integer idActividad, Integer idProfesor, MultipartFile imagen);

    /** Elimina la portada de la actividad sin subir otra. */
    SolicitudResponseDTO eliminarImagen(Integer idActividad, Integer idProfesor);
}