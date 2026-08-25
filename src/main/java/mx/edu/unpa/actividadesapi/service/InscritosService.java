package mx.edu.unpa.actividadesapi.service;
import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;

public interface InscritosService {
    ListaInscritosResponseDTO obtenerLista(Integer idActividad, Integer idSolicitante, boolean esAdmin);
    byte[] generarPdf(Integer idActividad, Integer idSolicitante, boolean esAdmin);
    byte[] generarCsv(Integer idActividad, Integer idSolicitante, boolean esAdmin);
}