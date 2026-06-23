package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.ActividadPublicaDTO;
import mx.edu.unpa.actividadesapi.dto.response.ActividadDetallePublicoResponse;
import mx.edu.unpa.actividadesapi.dto.response.EventoDestacadoResponse;
import mx.edu.unpa.actividadesapi.model.Actividad;
import mx.edu.unpa.actividadesapi.model.Categoria;


import java.util.List;
import java.util.stream.Collectors;

public interface CalendarioService {

    public List<ActividadPublicaDTO> getActividadesPublicas(Integer idCategoria);

    public List<Categoria> getCategorias();

    // Detalle completo de una actividad publica
    ActividadDetallePublicoResponse getDetalleActividad(Integer idActividad);

    // US-27: evento destacado activo para el banner publico (null si no hay)
    EventoDestacadoResponse getEventoDestacado();

}