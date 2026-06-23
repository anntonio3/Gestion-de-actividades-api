package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.AprobarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.request.DestacarRequest;
import mx.edu.unpa.actividadesapi.dto.request.RechazarActividadRequest;
import mx.edu.unpa.actividadesapi.dto.response.DestacarResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDecididaResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.vicerrectoria.SolicitudListItemResponse;
import mx.edu.unpa.actividadesapi.service.VicerrectoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vicerrectoria/solicitudes")
public class VicerrectoriaController {

    private static final Logger log = LoggerFactory.getLogger(VicerrectoriaController.class);

    private final VicerrectoriaService vicerrectoriaService;

    public VicerrectoriaController(VicerrectoriaService vicerrectoriaService) {
        this.vicerrectoriaService = vicerrectoriaService;
    }

    /**
     * US-07: Lista todas las solicitudes con filtros opcionales.
     * GET /api/vicerrectoria/solicitudes?estado=PENDIENTE&idCategoria=1&q=texto
     */
    @GetMapping
    public ResponseEntity<List<SolicitudListItemResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idCarrera,
            @RequestParam(required = false) Integer idDepartamento,
            @RequestParam(required = false) String q) {

        log.info("GET /api/vicerrectoria/solicitudes estado={} cat={} carr={} dept={} q={}",
                estado, idCategoria, idCarrera, idDepartamento, q);
        List<SolicitudListItemResponse> lista = vicerrectoriaService
                .listarSolicitudes(estado, idCategoria, idCarrera, idDepartamento, q);
        return ResponseEntity.ok(lista);
    }

    /**
     * US-10: Detalle completo de una solicitud, incluyendo conflictos.
     * GET /api/vicerrectoria/solicitudes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDetalleResponse> detalle(@PathVariable Integer id) {
        log.info("GET /api/vicerrectoria/solicitudes/{}", id);
        return ResponseEntity.ok(vicerrectoriaService.obtenerDetalle(id));
    }

    /**
     * US-08: Aprueba la solicitud (cambia estado a APROBADA).
     * POST /api/vicerrectoria/solicitudes/{id}/aprobar
     */
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudDecididaResponse> aprobar(
            @PathVariable Integer id,
            @Valid @RequestBody AprobarActividadRequest request) {
        log.info("POST /api/vicerrectoria/solicitudes/{}/aprobar admin={}", id, request.getIdAdmin());
        return ResponseEntity.ok(vicerrectoriaService.aprobar(id, request));
    }

    /**
     * US-09: Rechaza la solicitud con motivo obligatorio.
     * POST /api/vicerrectoria/solicitudes/{id}/rechazar
     */
    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudDecididaResponse> rechazar(
            @PathVariable Integer id,
            @Valid @RequestBody RechazarActividadRequest request) {
        log.info("POST /api/vicerrectoria/solicitudes/{}/rechazar admin={}", id, request.getIdAdmin());
        return ResponseEntity.ok(vicerrectoriaService.rechazar(id, request));
    }

    /**
     * US-26: Marca una actividad aprobada como destacada (doble confirmacion en front).
     * Si ya hay otro destacado y confirmarReemplazo=false, responde 409 con los datos del actual.
     * POST /api/vicerrectoria/solicitudes/{id}/destacar
     */
    @PostMapping("/{id}/destacar")
    public ResponseEntity<DestacarResponse> destacar(
            @PathVariable Integer id,
            @Valid @RequestBody DestacarRequest request) {
        log.info("POST /api/vicerrectoria/solicitudes/{}/destacar admin={}", id, request.getIdAdmin());
        return ResponseEntity.ok(vicerrectoriaService.destacar(id, request));
    }

    /**
     * US-26: Quita el destacado de una actividad.
     * DELETE /api/vicerrectoria/solicitudes/{id}/destacar?idAdmin=X
     */
    @DeleteMapping("/{id}/destacar")
    public ResponseEntity<Void> quitarDestacado(
            @PathVariable Integer id,
            @RequestParam Integer idAdmin) {
        log.info("DELETE /api/vicerrectoria/solicitudes/{}/destacar admin={}", id, idAdmin);
        vicerrectoriaService.quitarDestacado(id, idAdmin);
        return ResponseEntity.noContent().build();
    }
}
