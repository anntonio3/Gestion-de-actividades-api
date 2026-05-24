package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.AvisoRequest;
import mx.edu.unpa.actividadesapi.dto.response.AvisoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface AvisoService {

    // US-17: Crear aviso (se publica automaticamente)
    AvisoResponse crear(AvisoRequest request, MultipartFile foto);

    // US-19: Editar aviso del profesor
    AvisoResponse actualizar(Integer idAviso, AvisoRequest request, MultipartFile foto);

    // Soft delete (extra, util para que el profesor retire un aviso que ya no aplica)
    void desactivar(Integer idAviso, Integer idProfesor);

    // US-19: Listado de avisos del profesor para que vea/edite los suyos
    List<AvisoResponse> listarPorProfesor(Integer idProfesor);

    // US-18/US-20: Listado publico (sin login). Filtros opcionales por fecha.
    List<AvisoResponse> listarPublico(LocalDate fecha, LocalDate desde, LocalDate hasta);

    // Detalle individual
    AvisoResponse obtenerPorId(Integer idAviso);
}
