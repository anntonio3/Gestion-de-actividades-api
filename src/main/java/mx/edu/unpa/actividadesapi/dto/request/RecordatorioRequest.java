package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * US-07: Request para disparar el envío de recordatorios.
 */
@Data
public class RecordatorioRequest {

    @NotNull(message = "El id del usuario es obligatorio.")
    private Integer idUsuario;
}