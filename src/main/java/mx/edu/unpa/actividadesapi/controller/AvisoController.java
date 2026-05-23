package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.AvisoRequest;
import mx.edu.unpa.actividadesapi.dto.response.AvisoResponse;
import mx.edu.unpa.actividadesapi.service.AvisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoints privados del profesor para gestionar sus avisos.
 * El listado publico vive en CorchoController para que se pueda
 * exponer con CORS abierto sin abrir tambien crear/editar/borrar.
 */
@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private static final Logger log = LoggerFactory.getLogger(AvisoController.class);

    @Autowired
    private AvisoService avisoService;

    // US-17: Crear aviso
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvisoResponse> crear(
            @Valid @ModelAttribute AvisoRequest request,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        log.info("POST /api/avisos profesor={} titulo='{}'",
                request.getIdProfesor(), request.getTitulo());
        AvisoResponse creado = avisoService.crear(request, foto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // US-19: Listar avisos del profesor (para que vea/edite los suyos)
    @GetMapping("/mis-avisos")
    public ResponseEntity<List<AvisoResponse>> misAvisos(
            @RequestParam Integer idProfesor) {

        log.info("GET /api/avisos/mis-avisos profesor={}", idProfesor);
        return ResponseEntity.ok(avisoService.listarPorProfesor(idProfesor));
    }

    // Detalle individual (lo usa el modal de edicion para precargar)
    @GetMapping("/{id}")
    public ResponseEntity<AvisoResponse> obtenerPorId(@PathVariable Integer id) {
        log.info("GET /api/avisos/{}", id);
        return ResponseEntity.ok(avisoService.obtenerPorId(id));
    }

    // US-19: Editar aviso
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvisoResponse> actualizar(
            @PathVariable Integer id,
            @Valid @ModelAttribute AvisoRequest request,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        log.info("PUT /api/avisos/{} profesor={}", id, request.getIdProfesor());
        return ResponseEntity.ok(avisoService.actualizar(id, request, foto));
    }

    // Retirar aviso (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Integer id,
            @RequestParam Integer idProfesor) {

        log.info("DELETE /api/avisos/{} profesor={}", id, idProfesor);
        avisoService.desactivar(id, idProfesor);
        return ResponseEntity.noContent().build();
    }
}
