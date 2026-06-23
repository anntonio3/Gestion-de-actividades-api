package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * US-26: Petición para marcar una actividad como destacada.
 * confirmarReemplazo:
 *   - false (default): si ya hay otro destacado activo, el back responde 409
 *     con los datos del actual para que el front pida confirmacion.
 *   - true: el admin ya confirmo; se desactiva el anterior y se activa este.
 */
@Data
public class DestacarRequest {

    @NotNull(message = "El id del administrador es obligatorio")
    private Integer idAdmin;

    @NotNull(message = "El indicador de confirmacion de reemplazo es obligatorio")
    private Boolean confirmarReemplazo = false;
}
