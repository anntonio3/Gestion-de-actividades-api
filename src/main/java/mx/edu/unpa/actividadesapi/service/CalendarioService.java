package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.Categoria;


import java.util.List;
import java.util.stream.Collectors;

public interface CalendarioService {

    public List<ActividadPublicaDTO> getActividadesPublicas(Integer idCategoria);

    public List<Categoria> getCategorias();

}