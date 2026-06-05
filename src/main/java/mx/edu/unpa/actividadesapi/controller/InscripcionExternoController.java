package mx.edu.unpa.actividadesapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.InscripcionExternoRequest;
import mx.edu.unpa.actividadesapi.dto.response.InscripcionExternoResponse;
import mx.edu.unpa.actividadesapi.service.InscripcionExternoService;
import mx.edu.unpa.actividadesapi.util.CookieVisitanteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * US-24: Endpoints para inscripcion de personas externas a la institucion.
 * La identificacion del visitante se basa en la cookie HTTP-only visitante_id
 * (mismo mecanismo que US-12 asistencia).
 */
@RestController
@RequestMapping("/api/inscripciones/externo")
public class InscripcionExternoController {

    private static final Logger log =
            LoggerFactory.getLogger(InscripcionExternoController.class);

    @Autowired
    private InscripcionExternoService inscripcionExternoService;

    /**
     * US-24: Inscribir a una persona externa en una actividad.
     * POST /api/inscripciones/externo/{idActividad}
     */
    @PostMapping("/{idActividad}")
    public ResponseEntity<InscripcionExternoResponse> inscribir(
            @PathVariable Integer idActividad,
            @Valid @RequestBody InscripcionExternoRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String idVisitante = CookieVisitanteHelper.obtenerOCrear(httpRequest, httpResponse);

        log.info("POST /api/inscripciones/externo/{} visitante={} nombre='{}'",
                idActividad, idVisitante, request.getNombre());

        InscripcionExternoResponse response =
                inscripcionExternoService.inscribir(idActividad, idVisitante, request);

        log.info("Externo inscrito exitosamente en actividad id={} idInscripcion={}",
                idActividad, response.getIdInscripcionExterno());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * US-24: Cancelar la inscripcion de un externo en una actividad.
     * Solo es posible si el evento aun no ha iniciado.
     * DELETE /api/inscripciones/externo/{idActividad}
     */
    @DeleteMapping("/{idActividad}")
    public ResponseEntity<Void> cancelar(
            @PathVariable Integer idActividad,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String idVisitante = CookieVisitanteHelper.obtenerOCrear(httpRequest, httpResponse);

        log.info("DELETE /api/inscripciones/externo/{} visitante={}", idActividad, idVisitante);

        inscripcionExternoService.cancelar(idActividad, idVisitante);

        log.info("Inscripcion externo cancelada actividad={} visitante={}", idActividad, idVisitante);

        return ResponseEntity.noContent().build();
    }

    /**
     * US-24: Total de externos inscritos en una actividad.
     * GET /api/inscripciones/externo/{idActividad}/total
     */
    @GetMapping("/{idActividad}/total")
    public ResponseEntity<Map<String, Integer>> totalExternos(
            @PathVariable Integer idActividad) {

        log.info("GET /api/inscripciones/externo/{}/total", idActividad);
        return ResponseEntity.ok(
                Map.of("total", inscripcionExternoService.totalExternos(idActividad)));
    }

    /**
     * US-24: Estado de inscripcion del visitante actual en una actividad.
     * GET /api/inscripciones/externo/{idActividad}/estado
     */
    @GetMapping("/{idActividad}/estado")
    public ResponseEntity<Map<String, Object>> estado(
            @PathVariable Integer idActividad,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String idVisitante = CookieVisitanteHelper.obtenerOCrear(httpRequest, httpResponse);

        boolean inscrito = inscripcionExternoService.estaInscrito(idActividad, idVisitante);
        int total        = inscripcionExternoService.totalExternos(idActividad);

        return ResponseEntity.ok(Map.of(
                "inscrito", inscrito,
                "total",    total
        ));
    }
}
