package mx.edu.unpa.actividadesapi.controller;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.InscritoListaItemResponse;
import mx.edu.unpa.actividadesapi.service.InscritosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * US-04: Endpoints para la lista de inscritos de una actividad.
 *
 * GET  /api/actividades/{id}/inscritos        → lista JSON
 * GET  /api/actividades/{id}/inscritos/pdf    → descarga PDF
 * GET  /api/actividades/{id}/inscritos/csv    → descarga CSV
 *
 * El parámetro idSolicitante identifica al docente/admin que hace la petición.
 * Spring Security ya valida que el usuario esté autenticado (PROFESOR o ADMIN).
 * El servicio valida además que sea el propietario o un ADMIN.
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class InscritosController {

    private static final Logger log = LoggerFactory.getLogger(InscritosController.class);
    private final InscritosService inscritosService;

    /** Lista JSON de inscritos para mostrar la tabla en el panel del docente. */
    @GetMapping("/{idActividad}/inscritos")
    public ResponseEntity<List<InscritoListaItemResponse>> listar(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante) {

        log.info("GET /api/actividades/{}/inscritos solicitante={}", idActividad, idSolicitante);
        return ResponseEntity.ok(inscritosService.obtenerLista(idActividad, idSolicitante));
    }

    /** Descarga el PDF con membrete y tabla de inscritos. */
    @GetMapping("/{idActividad}/inscritos/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante) {

        log.info("GET /api/actividades/{}/inscritos/pdf solicitante={}", idActividad, idSolicitante);
        byte[] pdf = inscritosService.generarPdf(idActividad, idSolicitante);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"inscritos-actividad-" + idActividad + ".pdf\"")
                .body(pdf);
    }

    /** Descarga el CSV con BOM UTF-8 (compatible con Excel). */
    @GetMapping("/{idActividad}/inscritos/csv")
    public ResponseEntity<byte[]> descargarCsv(
            @PathVariable Integer idActividad,
            @RequestParam Integer idSolicitante) {

        log.info("GET /api/actividades/{}/inscritos/csv solicitante={}", idActividad, idSolicitante);
        byte[] csv = inscritosService.generarCsv(idActividad, idSolicitante);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"inscritos-actividad-" + idActividad + ".csv\"")
                .body(csv);
    }
}