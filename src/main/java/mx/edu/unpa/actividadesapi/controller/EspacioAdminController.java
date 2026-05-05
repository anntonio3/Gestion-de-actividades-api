package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.EspacioEstadoRequest;
import mx.edu.unpa.actividadesapi.dto.request.EspacioRequest;
import mx.edu.unpa.actividadesapi.dto.response.EspacioDetalleResponse;
import mx.edu.unpa.actividadesapi.dto.response.MapaPuntoResponse;
import mx.edu.unpa.actividadesapi.service.EspacioAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de administración de espacios físicos (US-14).
 * El admin gestiona el mapa interactivo de la UNPA.
 *
 * El CORS se configura globalmente en WebConfig.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class EspacioAdminController {

    private static final Logger log = LoggerFactory.getLogger(EspacioAdminController.class);

    private final EspacioAdminService espacioAdminService;

    /** Puntos del mapa para pintar los círculos sobre la imagen. */
    @GetMapping("/mapa/puntos")
    public ResponseEntity<List<MapaPuntoResponse>> listarPuntos() {
        log.info("GET /api/admin/mapa/puntos");
        return ResponseEntity.ok(espacioAdminService.listarPuntosMapa());
    }

    /** Detalle de un espacio (para abrir el modal de edición). */
    @GetMapping("/espacios/{id}")
    public ResponseEntity<EspacioDetalleResponse> obtenerDetalle(@PathVariable Integer id) {
        log.info("GET /api/admin/espacios/{}", id);
        return ResponseEntity.ok(espacioAdminService.obtenerDetalle(id));
    }

    /** Registra un nuevo espacio con su equipamiento. */
    @PostMapping("/espacios")
    public ResponseEntity<EspacioDetalleResponse> registrar(
            @Valid @RequestBody EspacioRequest request) {
        log.info("POST /api/admin/espacios punto={} nombre='{}'",
                request.getIdPunto(), request.getNombre());
        EspacioDetalleResponse response = espacioAdminService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Actualiza datos del espacio y reescribe la lista de equipamiento. */
    @PutMapping("/espacios/{id}")
    public ResponseEntity<EspacioDetalleResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EspacioRequest request) {
        log.info("PUT /api/admin/espacios/{}", id);
        return ResponseEntity.ok(espacioAdminService.actualizar(id, request));
    }

    /** Activa o desactiva un espacio (delete lógico). */
    @PatchMapping("/espacios/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody EspacioEstadoRequest request) {
        log.info("PATCH /api/admin/espacios/{}/estado activo={}", id, request.getActivo());
        espacioAdminService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.noContent().build();
    }
}
