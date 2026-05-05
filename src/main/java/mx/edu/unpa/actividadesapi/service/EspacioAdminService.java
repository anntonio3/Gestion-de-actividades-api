package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.EspacioRequest;
import mx.edu.unpa.actividadesapi.dto.response.EspacioDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.MapaPuntoResponse;

import java.util.List;

public interface EspacioAdminService {

    /** Lista todos los puntos del mapa con info del espacio asignado (si lo hay). */
    List<MapaPuntoResponse> listarPuntosMapa();

    /** Detalle completo de un espacio para edición. */
    EspacioDetalleResponse obtenerDetalle(Integer idEspacio);

    /** Registra un nuevo espacio asociado a un punto del mapa. */
    EspacioDetalleResponse registrar(EspacioRequest request);

    /** Actualiza un espacio existente. Reescribe la lista de equipamiento. */
    EspacioDetalleResponse actualizar(Integer idEspacio, EspacioRequest request);

    /** Cambia el estado activo/inactivo del espacio. */
    void cambiarEstado(Integer idEspacio, Boolean activo);
}
