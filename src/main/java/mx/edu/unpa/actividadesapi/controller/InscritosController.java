package mx.edu.unpa.actividadesapi.controller;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.response.ActividadInscripcionResumenDTO;
import mx.edu.unpa.actividadesapi.dto.response.ListaInscritosResponseDTO;
import mx.edu.unpa.actividadesapi.service.InscritosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * US-28: Endpoints para la lista de inscritos de una actividad.
 *
 * GET  /api/actividades/{id}/inscritos       -> lista JSON
 * GET  /api/actividades/{id}/inscritos/pdf   -> descarga PDF
 * GET  /api/actividades/{id}/inscritos/csv   -> descarga CSV
 *
 * SecurityConfig ya restringe toda la ruta a PROFESOR o ADMIN.
 * Aqui solo extraemos el id y rol del token para que el service
 * valide que sea el propietario del evento o un ADMIN.
 */
@RestController
@RequestMapping("/api/actividades")
public class InscritosController {

    private static final Logger log = LoggerFactory.getLogger(InscritosController.class);

    @Autowired
    private InscritosService inscritosService;

    @GetMapping("/{idActividad}/inscritos")
    public ResponseEntity<ListaInscritosResponseDTO> listar(
            @PathVariable Integer idActividad,
            Authentication authentication) {

        Integer idSolicitante = obtenerIdSolicitante(authentication);
        boolean esAdmin = esAdmin(authentication);

        log.info("GET /api/actividades/{}/inscritos solicitante={} esAdmin={}",
                idActividad, idSolicitante, esAdmin);

        return ResponseEntity.ok(inscritosService.obtenerLista(idActividad, idSolicitante, esAdmin));
    }

    @GetMapping("/{idActividad}/inscritos/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable Integer idActividad,
            Authentication authentication) {

        Integer idSolicitante = obtenerIdSolicitante(authentication);
        boolean esAdmin = esAdmin(authentication);

        log.info("GET /api/actividades/{}/inscritos/pdf solicitante={}", idActividad, idSolicitante);

        byte[] pdf = inscritosService.generarPdf(idActividad, idSolicitante, esAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"inscritos-actividad-" + idActividad + ".pdf\"");

        return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/{idActividad}/inscritos/csv")
    public ResponseEntity<byte[]> descargarCsv(
            @PathVariable Integer idActividad,
            Authentication authentication) {

        Integer idSolicitante = obtenerIdSolicitante(authentication);
        boolean esAdmin = esAdmin(authentication);

        log.info("GET /api/actividades/{}/inscritos/csv solicitante={}", idActividad, idSolicitante);

        byte[] csv = inscritosService.generarCsv(idActividad, idSolicitante, esAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"inscritos-actividad-" + idActividad + ".csv\"");

        return new ResponseEntity<>(csv, headers, org.springframework.http.HttpStatus.OK);
    }

    // Dashboard ADMIN: todas las actividades aprobadas con inscripcion (US-28)
    @GetMapping("/admin/inscripcion")
    public ResponseEntity<List<ActividadInscripcionResumenDTO>> listarParaAdmin() {
        log.info("GET /api/actividades/admin/inscripcion");
        return ResponseEntity.ok(inscritosService.listarActividadesConInscripcion());
    }

    // ── Helpers privados para leer el JWT ya validado por JwtFilter ──

    private Integer obtenerIdSolicitante(Authentication authentication) {
        // El JwtFilter pone el id (Integer) como principal del Authentication
        return (Integer) authentication.getPrincipal();
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}