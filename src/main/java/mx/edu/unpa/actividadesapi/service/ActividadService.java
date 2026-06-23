package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.ActividadRequest;
import mx.edu.unpa.actividadesapi.dto.response.ActividadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ActividadService {
    ActividadResponse registrarActividad(ActividadRequest request, MultipartFile portada);

}
