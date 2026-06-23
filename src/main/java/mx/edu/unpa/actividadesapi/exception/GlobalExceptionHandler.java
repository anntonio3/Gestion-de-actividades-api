package mx.edu.unpa.actividadesapi.exception;

import jakarta.persistence.OptimisticLockException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
    log.warn("Recurso no encontrado: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
    log.warn("Regla de negocio violada: {}", ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  // Optimistic locking: alguien mas modifico la entidad
  @ExceptionHandler({
          ConcurrenciaException.class,
          OptimisticLockException.class,
          OptimisticLockingFailureException.class
  })
  public ResponseEntity<Map<String, Object>> handleConcurrencia(Exception ex) {
    log.warn("Conflicto de concurrencia: {}", ex.getMessage());
    return buildResponse(HttpStatus.CONFLICT,
            "Esta actividad fue modificada por otro usuario. Por favor recarga e intenta de nuevo.");
  }

  // Maneja errores de @Valid en los DTOs
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errores = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String campo = ((FieldError) error).getField();
      String mensaje = error.getDefaultMessage();
      errores.put(campo, mensaje);
    });
    log.warn("Error de validación: {}", errores);
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("errores", errores);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    log.error("Error inesperado: {}", ex.getMessage(), ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
  }

  // US-26: conflicto al destacar cuando ya hay otro activo sin confirmar reemplazo
  @ExceptionHandler(DestacadoConflictoException.class)
  public ResponseEntity<Map<String, Object>> handleDestacadoConflicto(DestacadoConflictoException ex) {
    log.warn("Conflicto de destacado: {}", ex.getMessage());
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("mensaje", ex.getMessage());
    // Datos del destacado actual para que el front arme la advertencia de reemplazo
    body.put("idDestacadoActual", ex.getIdActual());
    body.put("nombreDestacadoActual", ex.getNombreActual());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("mensaje", mensaje);
    return ResponseEntity.status(status).body(body);
  }
}
