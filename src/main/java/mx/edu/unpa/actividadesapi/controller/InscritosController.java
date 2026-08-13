package mx.edu.unpa.actividadesapi.controller;

import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;
import mx.edu.unpa.actividadesapi.service.InscritosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscritos")
public class InscritosController {

    @Autowired
    private InscritosService inscritosService;

    // US-28: Consultar lista de inscritos
    @GetMapping("/{idActividad}")
    public ResponseEntity<ListaInscritosResponseDTO> listar(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante,
            @RequestParam String rolSolicitante) {

        return ResponseEntity.ok(inscritosService.obtenerLista(idActividad, idSolicitante, rolSolicitante));
    }

    // US-28: Descargar PDF
    @GetMapping("/{idActividad}/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante,
            @RequestParam String rolSolicitante) {

        byte[] pdf = inscritosService.generarPdf(idActividad, idSolicitante, rolSolicitante);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("inscritos_actividad_" + idActividad + ".pdf")
                .build());

        return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
    }

    // US-28: Descargar CSV
    @GetMapping("/{idActividad}/csv")
    public ResponseEntity<byte[]> descargarCsv(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante,
            @RequestParam String rolSolicitante) {

        byte[] csv = inscritosService.generarCsv(idActividad, idSolicitante, rolSolicitante);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition
                .attachment()
                .filename("inscritos_actividad_" + idActividad + ".csv")
                .build());

        return new ResponseEntity<>(csv, headers, org.springframework.http.HttpStatus.OK);
    }
}