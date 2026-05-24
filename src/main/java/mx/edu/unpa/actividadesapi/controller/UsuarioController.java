package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioCrearRequest;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioEditarRequest;
import mx.edu.unpa.actividadesapi.dto.request.UsuarioEstadoRequest;
import mx.edu.unpa.actividadesapi.dto.response.UsuarioResponse;
import mx.edu.unpa.actividadesapi.enums.Rol;
import mx.edu.unpa.actividadesapi.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de administración de usuarios (US-01).
 * El CORS se configura globalmente en WebConfig.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;

    /**
     * US-01: Listar usuarios con filtros opcionales.
     * GET /api/admin/usuarios
     * GET /api/admin/usuarios?rol=PROFESOR
     * GET /api/admin/usuarios?activo=true
     * GET /api/admin/usuarios?q=carlos
     */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String q) {

        log.info("GET /api/admin/usuarios rol={} activo={} q={}", rol, activo, q);
        return ResponseEntity.ok(usuarioService.listar(rol, activo, q));
    }

    /**
     * US-01: Obtener detalle de un usuario.
     * GET /api/admin/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Integer id) {
        log.info("GET /api/admin/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /**
     * US-01: Crear nuevo usuario.
     * La contraseña inicial se genera automáticamente como la parte
     * anterior al @ del correo.
     * POST /api/admin/usuarios
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @Valid @RequestBody UsuarioCrearRequest request) {

        log.info("POST /api/admin/usuarios correo={} rol={}", request.getCorreo(), request.getRol());
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * US-01: Editar datos del usuario (nombre, apellidos, correo, rol).
     * La contraseña no se edita desde este endpoint.
     * PUT /api/admin/usuarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> editar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioEditarRequest request) {

        log.info("PUT /api/admin/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.editar(id, request));
    }

    /**
     * US-01: Activar o desactivar un usuario (delete lógico).
     * PATCH /api/admin/usuarios/{id}/estado
     * body: { "activo": false }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioEstadoRequest request) {

        log.info("PATCH /api/admin/usuarios/{}/estado activo={}", id, request.getActivo());
        usuarioService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.noContent().build();
    }
}
