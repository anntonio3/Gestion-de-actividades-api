package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;

public interface InscritosService {

    ListaInscritosResponseDTO obtenerLista(Integer idActividad, Integer idSolicitante, String rolSolicitante);

    byte[] generarPdf(Integer idActividad, Integer idSolicitante, String rolSolicitante);

    byte[] generarCsv(Integer idActividad, Integer idSolicitante, String rolSolicitante);
}
