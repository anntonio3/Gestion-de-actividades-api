package mx.edu.unpa.actividadesapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.AsistenciaRequest;
import mx.edu.unpa.actividadesapi.dto.response.AsistenciaResponse;
import mx.edu.unpa.actividadesapi.service.AsistenciaService;
import mx.edu.unpa.actividadesapi.util.CookieVisitanteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asistencia")
public class AsistenciaController {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaController.class);

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    /**
     * US-12: Devuelve conteos + respuesta del visitante para varias actividades.
     * Se usa al cargar el calendario publico.
     * GET /api/asistencia?ids=1,2,3
     */
    @GetMapping
    public ResponseEntity<Map<Integer, AsistenciaResponse>> obtenerLote(
            @RequestParam List<Integer> ids,
            HttpServletRequest req,
            HttpServletResponse res) {
        String idVisitante = CookieVisitanteHelper.obtenerOCrear(req, res);
        return ResponseEntity.ok(asistenciaService.obtenerConteosEnLote(ids, idVisitante));
    }

    /**
     * US-12: Estado para una sola actividad (modal/detalle).
     * GET /api/asistencia/{idActividad}
     */
    @GetMapping("/{idActividad}")
    public ResponseEntity<AsistenciaResponse> obtener(
            @PathVariable Integer idActividad,
            HttpServletRequest req,
            HttpServletResponse res) {
        String idVisitante = CookieVisitanteHelper.obtenerOCrear(req, res);
        return ResponseEntity.ok(asistenciaService.obtenerEstado(idActividad, idVisitante));
    }

    /**
     * US-12: Registra o actualiza la respuesta del visitante.
     * POST /api/asistencia/{idActividad}
     * body: { "respuesta": "VOY" | "TAL_VEZ" | "NO_VOY" }
     */
    @PostMapping("/{idActividad}")
    public ResponseEntity<AsistenciaResponse> responder(
            @PathVariable Integer idActividad,
            @Valid @RequestBody AsistenciaRequest request,
            HttpServletRequest req,
            HttpServletResponse res) {
        String idVisitante = CookieVisitanteHelper.obtenerOCrear(req, res);
        log.info("POST /api/asistencia/{} respuesta={}", idActividad, request.getRespuesta());
        return ResponseEntity.ok(
                asistenciaService.responder(idActividad, idVisitante, request.getRespuesta()));
    }
}