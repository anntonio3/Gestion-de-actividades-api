package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Cuerpo del POST /api/auth/login.
 * El campo "identificador" acepta matrícula (alumno) o correo (profesor/admin).
 */
@Data
public class LoginRequest {

    @NotBlank(message = "El identificador es obligatorio")
    private String identificador;

    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;
}
