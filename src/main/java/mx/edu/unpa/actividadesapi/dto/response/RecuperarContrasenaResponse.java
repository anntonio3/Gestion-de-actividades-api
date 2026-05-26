package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

/**
 * Respuesta del POST /api/auth/recuperar.
 * En modo simulación incluye el token para que el equipo
 * pueda probar el flujo sin servidor de correo.
 * En producción con SMTP real, tokenSimulado será null.
 */
@Data
public class RecuperarContrasenaResponse {

    private String mensaje;

    // Solo en modo desarrollo/simulación.
    // En producción con SMTP configurado, este campo es null.
    private String tokenSimulado;
}
