package mx.edu.unpa.actividadesapi.service;


import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCarreraDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaCampusDTO;
import mx.edu.unpa.actividadesapi.dto.response.estadisticas.EstadisticaMesDTO;

import java.util.List;

/**
 * Servicio de estadísticas para US-21, US-22 y US-23.
 */
public interface EstadisticasService {

    /** US-21: Cantidad de eventos por mes y año, en general. */
    List<EstadisticaMesDTO> obtenerGeneral();

    /** US-22: Cantidad de eventos por mes y año desglosado por campus (departamento). */
    List<EstadisticaCampusDTO> obtenerPorCampus();

    /** US-23: Cantidad de eventos por mes y año desglosado por carrera. */
    List<EstadisticaCarreraDTO> obtenerPorCarrera();
}
