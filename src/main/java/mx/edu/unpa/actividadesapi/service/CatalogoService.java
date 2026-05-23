package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.response.catalogo.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CatalogoService {
    List<CategoriaResponse> getCategorias();
    List<TipoActividadResponse> getTiposActividad(Integer categoriaId);
    List<DepartamentoResponse> getDepartamentos();
    List<CarreraResponse> getCarreras();
    List<EspacioResponse> getEspacios(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
    List<MobiliarioResponse> getMobiliario(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin);
    List<CampusResponse> getCampus();
}
