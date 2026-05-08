package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.InmobiliarioRequest;
import mx.edu.unpa.actividadesapi.dto.response.InmobiliarioResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InmobiliarioService {

    // US-14: Registrar nuevo inmobiliario
    InmobiliarioResponse crear(InmobiliarioRequest request, MultipartFile foto);

    // Listar todo el inmobiliario activo (búsqueda opcional por nombre)
    List<InmobiliarioResponse> listar(String nombre);

    // Obtener uno por ID
    InmobiliarioResponse obtenerPorId(Integer id);

    // Actualizar (foto opcional: si no llega se conserva la anterior)
    InmobiliarioResponse actualizar(Integer id, InmobiliarioRequest request, MultipartFile foto);

    // Baja lógica (activo = false)
    void desactivar(Integer id);
}