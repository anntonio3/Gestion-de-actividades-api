package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.AprobarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.request.DestacarRequest;
import mx.edu.unpa.actividadesapi.dto.request.RechazarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.response.DestacarResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDecididaResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudListItemResponse;

import java.util.List;

public interface VicerrectoriaService {

    // US-07: Listado con filtros opcionales
    List<SolicitudListItemResponse> listarSolicitudes(
            String estado,
            Integer idCategoria,
            Integer idCarrera,
            Integer idDepartamento,
            String busqueda
    );

    // US-10: Detalle completo + conflictos detectados
    SolicitudDetalleResponse obtenerDetalle(Integer idActividad);

    // US-08: Aprobar
    SolicitudDecididaResponse aprobar(Integer idActividad, AprobarActividadRequest request);

    // US-09: Rechazar con motivo
    SolicitudDecididaResponse rechazar(Integer idActividad, RechazarActividadRequest request);

    // US-26: Marcar una actividad aprobada como destacada (doble confirmacion en front).
    // Si ya hay otro destacado y confirmarReemplazo=false, lanza DestacadoConflictoException (409).
    DestacarResponse destacar(Integer idActividad, DestacarRequest request);

    // US-26: Quitar el destacado de una actividad.
    void quitarDestacado(Integer idActividad, Integer idAdmin);
}