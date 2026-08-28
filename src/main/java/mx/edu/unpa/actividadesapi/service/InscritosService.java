package mx.edu.unpa.actividadesapi.service;
import mx.edu.unpa.actividadesapi.dto.response.ActividadInscripcionResumenDTO;
import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;

import java.util.List;

public interface InscritosService {
    ListaInscritosResponseDTO obtenerLista(Integer idActividad, Integer idSolicitante, boolean esAdmin);
    byte[] generarPdf(Integer idActividad, Integer idSolicitante, boolean esAdmin);
    byte[] generarCsv(Integer idActividad, Integer idSolicitante, boolean esAdmin);
    List<ActividadInscripcionResumenDTO> listarActividadesConInscripcion();
}