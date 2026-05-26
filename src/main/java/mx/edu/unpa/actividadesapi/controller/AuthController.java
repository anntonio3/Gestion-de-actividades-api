package mx.edu.unpa.actividadesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.*;
import mx.edu.unpa.actividadesapi.dto.response.LoginResponse;
import mx.edu.unpa.actividadesapi.dto.response.RecuperarContrasenaResponse;
import mx.edu.unpa.actividadesapi.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Login unificado para alumnos (matrícula), profesores y admins (correo).
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login identificador={}", request.getIdentificador());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Registro de cuenta para alumnos únicamente.
     * POST /api/auth/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(
            @Valid @RequestBody RegistroAlumnoRequest request) {
        log.info("POST /api/auth/registro matricula={}", request.getMatricula());
        LoginResponse response = authService.registrarAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Solicitar correo de recuperación de contraseña.
     * POST /api/auth/recuperar
     */
    @PostMapping("/recuperar")
    public ResponseEntity<RecuperarContrasenaResponse> recuperar(
            @Valid @RequestBody RecuperarContrasenaRequest request) {
        log.info("POST /api/auth/recuperar correo={}", request.getCorreo());
        return ResponseEntity.ok(authService.solicitarRecuperacion(request));
    }

    /**
     * Verifica que el token de recuperación sea válido y no haya expirado.
     * El frontend llama este endpoint al cargar la pantalla de restablecimiento.
     * GET /api/auth/verificar-token/{token}
     */
    @GetMapping("/verificar-token/{token}")
    public ResponseEntity<Map<String, String>> verificarToken(@PathVariable String token) {
        log.info("GET /api/auth/verificar-token");
        authService.verificarToken(token);
        return ResponseEntity.ok(Map.of("mensaje", "Token válido."));
    }

    /**
     * Restablece la contraseña usando el token recibido por correo.
     * POST /api/auth/restablecer
     */
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @Valid @RequestBody RestablecerContrasenaRequest request) {
        log.info("POST /api/auth/restablecer");
        authService.restablecerContrasena(request);
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
    }

    /**
     * Logout — sin JWT simplemente confirma al frontend.
     * El frontend limpia el estado de sesión local.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("POST /api/auth/logout");
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada."));
    }
}
