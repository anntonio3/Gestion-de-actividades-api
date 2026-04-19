package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.repository.ActividadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CalendarioService {

    @Autowired
    private ActividadRepository repo;

    public List<ActividadPublicaDTO> getActividadesPublicas(Integer idTipo) {
        List<Actividad> lista = (idTipo != null)
                ? repo.findByEstadoAndTipo_IdTipo(Actividad.EstadoActividad.APROBADA, idTipo)
                : repo.findByEstado(Actividad.EstadoActividad.APROBADA);

        return lista.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ActividadPublicaDTO toDTO(Actividad a) {
        ActividadPublicaDTO dto = new ActividadPublicaDTO();
        dto.setId(a.getIdActividad());
        dto.setNombre(a.getNombre());
        dto.setDescripcion(a.getDescripcion());
        dto.setFecha(a.getFechaActividad());
        dto.setHoraInicio(a.getHoraInicio());
        dto.setHoraFin(a.getHoraFin());
        dto.setTipo(a.getTipo().getNombre());
        return dto;
    }
}
