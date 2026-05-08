package mx.edu.unpa.actividadesapi.controller;


import jakarta.validation.Valid;
import mx.edu.unpa.actividadesapi.dto.request.InmobiliarioRequest;
import mx.edu.unpa.actividadesapi.dto.response.InmobiliarioResponse;
import mx.edu.unpa.actividadesapi.service.InmobiliarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inmobiliario")
public class InmobiliarioController {

    private static final Logger log = LoggerFactory.getLogger(InmobiliarioController.class);

    @Autowired
    private InmobiliarioService inmobiliarioService;

    // ────────────────────────────────────────────────────────────────
    // US-14: Registrar nuevo inmobiliario
    // POST /api/admin/inmobiliario
    // Content-Type: multipart/form-data
    // ────────────────────────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InmobiliarioResponse> crear(
            @Valid @ModelAttribute InmobiliarioRequest request,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        log.info("POST /api/admin/inmobiliario nombre={}", request.getNombre());
        InmobiliarioResponse creado = inmobiliarioService.crear(request, foto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ────────────────────────────────────────────────────────────────
    // Listar inmobiliario activo (búsqueda opcional por nombre)
    // GET /api/admin/inmobiliario
    // GET /api/admin/inmobiliario?nombre=proyector
    // ────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<InmobiliarioResponse>> listar(
            @RequestParam(required = false) String nombre) {

        log.info("GET /api/admin/inmobiliario nombre={}", nombre);
        return ResponseEntity.ok(inmobiliarioService.listar(nombre));
    }

    // ────────────────────────────────────────────────────────────────
    // Obtener uno por ID
    // GET /api/admin/inmobiliario/{id}
    // ────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<InmobiliarioResponse> obtenerPorId(@PathVariable Integer id) {
        log.info("GET /api/admin/inmobiliario/{}", id);
        return ResponseEntity.ok(inmobiliarioService.obtenerPorId(id));
    }

    // ────────────────────────────────────────────────────────────────
    // Actualizar inmobiliario (foto opcional)
    // PUT /api/admin/inmobiliario/{id}
    // Content-Type: multipart/form-data
    // ────────────────────────────────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InmobiliarioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @ModelAttribute InmobiliarioRequest request,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        log.info("PUT /api/admin/inmobiliario/{}", id);
        return ResponseEntity.ok(inmobiliarioService.actualizar(id, request, foto));
    }

    // ────────────────────────────────────────────────────────────────
    // Dar de baja (soft delete)
    // DELETE /api/admin/inmobiliario/{id}
    // ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        log.info("DELETE /api/admin/inmobiliario/{}", id);
        inmobiliarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
