package mx.edu.unpa.actividadesapi.service.impl;

import jakarta.transaction.Transactional;
import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.ActividadImagen;
import mx.edu.unpa.actividadesapi.model.Categoria;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import mx.edu.unpa.actividadesapi.repository.CategoriaRepository;
import mx.edu.unpa.actividadesapi.service.CalendarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarioServiceImpl implements CalendarioService {

    @Autowired
    private ActividadRepository actividadRepo;
    @Autowired
    private CategoriaRepository categoriaRepo;

    @Override
    @Transactional
    public List<ActividadPublicaDTO> getActividadesPublicas(Integer idCategoria) {
        List<Actividad> lista = (idCategoria != null)
                ? actividadRepo.findByEstadoAndTipo_Categoria_IdCategoria(
                EstadoActividad.APROBADA, idCategoria)
                : actividadRepo.findByEstado(EstadoActividad.APROBADA);

        return lista.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Categoria> getCategorias() {
        return categoriaRepo.findAll();
    }

    private ActividadPublicaDTO toDTO(Actividad a) {
        ActividadPublicaDTO dto = new ActividadPublicaDTO();
        dto.setId(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setFechaActividad(a.getFechaActividad());
        dto.setHoraInicio(a.getHoraInicio());
        dto.setHoraFin(a.getHoraFin());
        dto.setTipo(a.getTipo().getNombre());
        dto.setCategoria(a.getTipo().getCategoria().getNombre()); // NUEVO

        // NUEVO: buscar imagen de portada
        if (a.getImagenes() != null) {
            a.getImagenes().stream()
                    .filter(ActividadImagen::getEsPortada)
                    .findFirst()
                    .ifPresent(img -> dto.setImagenPortada(img.getUrl()));
        }

        return dto;
    }
}
