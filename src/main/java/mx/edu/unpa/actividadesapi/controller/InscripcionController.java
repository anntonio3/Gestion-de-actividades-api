package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.InscripcionRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionEstadoResponse;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionResponse;
import mx.edu.unpa.actividadesapi.service.InscripcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inscripciones")
@RequiredArgsConstructor
public class InscripcionController {

    private static final Logger log = LoggerFactory.getLogger(InscripcionController.class);

    private final InscripcionService inscripcionService;

    /**
     * US-16: Inscribirse en una actividad.
     * POST /api/inscripciones/{idActividad}
     */
    @PostMapping("/{idActividad}")
    public ResponseEntity<InscripcionEstadoResponse> inscribir(
            @PathVariable Integer idActividad,
            @Valid @RequestBody InscripcionRequest request) {
        log.info("POST /api/inscripciones/{} actor={} tipo={}",
                idActividad, request.getIdActor(), request.getTipoUsuario());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscripcionService.inscribir(idActividad, request));
    }

    /**
     * US-16: Cancelar inscripcion.
     * DELETE /api/inscripciones/{idActividad}
     */
    @DeleteMapping("/{idActividad}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Integer idActividad,
            @Valid @RequestBody InscripcionRequest request) {
        log.info("DELETE /api/inscripciones/{} actor={} tipo={}",
                idActividad, request.getIdActor(), request.getTipoUsuario());
        inscripcionService.cancelar(idActividad, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * US-16: Listar mis inscripciones.
     * GET /api/inscripciones/mis-inscripciones?idActor=X&tipoUsuario=ALUMNO
     */
    @GetMapping("/mis-inscripciones")
    public ResponseEntity<List<InscripcionResponse>> misInscripciones(
            @RequestParam Integer idActor,
            @RequestParam String tipoUsuario) {
        log.info("GET /api/inscripciones/mis-inscripciones actor={} tipo={}", idActor, tipoUsuario);
        return ResponseEntity.ok(inscripcionService.misInscripciones(idActor, tipoUsuario));
    }

    /**
     * US-16: Estado de inscripcion del actor en una actividad.
     * GET /api/inscripciones/{idActividad}/estado?idActor=X&tipoUsuario=ALUMNO
     */
    @GetMapping("/{idActividad}/estado")
    public ResponseEntity<InscripcionEstadoResponse> obtenerEstado(
            @PathVariable Integer idActividad,
            @RequestParam Integer idActor,
            @RequestParam String tipoUsuario) {
        return ResponseEntity.ok(
                inscripcionService.obtenerEstado(idActividad, idActor, tipoUsuario));
    }

    /**
     * US-16: Estado en lote para el calendario.
     * GET /api/inscripciones/lote?ids=1,2,3&idActor=X&tipoUsuario=ALUMNO
     */
    @GetMapping("/lote")
    public ResponseEntity<Map<Integer, InscripcionEstadoResponse>> obtenerLote(
            @RequestParam List<Integer> ids,
            @RequestParam(required = false) Integer idActor,
            @RequestParam(required = false) String tipoUsuario) {
        return ResponseEntity.ok(
                inscripcionService.obtenerEstadoEnLote(ids, idActor, tipoUsuario));
    }

    /**
     * US-16: Total de inscritos en una actividad (para el profesor en mis-solicitudes).
     * GET /api/inscripciones/{idActividad}/total
     */
    @GetMapping("/{idActividad}/total")
    public ResponseEntity<Map<String, Integer>> totalInscritos(@PathVariable Integer idActividad) {
        return ResponseEntity.ok(Map.of("total", inscripcionService.totalInscritos(idActividad)));
    }
}