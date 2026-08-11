package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.response.InscritoListaItemResponse;

import java.util.List;

/**
 * US-04: Servicio para obtener la lista de inscritos de una actividad
 * y generar los documentos PDF y CSV.
 */
public interface InscritosService {

    /** Lista de inscritos (internos + externos), ordenada por fecha de inscripción. */
    List<InscritoListaItemResponse> obtenerLista(Integer idActividad, Integer idSolicitante);

    /** Genera el PDF como arreglo de bytes listo para enviar como descarga. */
    byte[] generarPdf(Integer idActividad, Integer idSolicitante);

    /** Genera el CSV como arreglo de bytes listo para enviar como descarga. */
    byte[] generarCsv(Integer idActividad, Integer idSolicitante);
}