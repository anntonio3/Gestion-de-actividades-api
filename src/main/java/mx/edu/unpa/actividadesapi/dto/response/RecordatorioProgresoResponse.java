package mx.edu.unpa.actividadesapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * US-07: Progreso de envío de recordatorios (SSE).
 * estado: "EN_PROGRESO" | "COMPLETADO" | "ERROR"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordatorioProgresoResponse {
    private int    enviados;
    private int    total;
    private String estado;
    private String mensaje;
}