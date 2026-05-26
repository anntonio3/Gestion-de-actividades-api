package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Cuerpo del POST /api/auth/recuperar.
 * Se busca el correo tanto en usuarios como en alumnos.
 */
@Data
public class RecuperarContrasenaRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String correo;
}
