package mx.edu.unpa.actividadesapi.dto.response.vicerrectoria;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.EstadoActividad;

import java.time.LocalDateTime;

/**
 * US-08/US-09: Respuesta tras aprobar o rechazar una solicitud.
 * Liviana — solo confirma el cambio de estado al frontend.
 */
@Data
public class SolicitudDecididaResponse {
    private Integer idActividad;
    private EstadoActividad estado;
    private String motivoRechazo;
    private LocalDateTime fechaRevision;
    private String nombreVicerrector;
    private Integer version;

    // US-25: true si el tipo de la actividad es DESTACADO y conviene
    // sugerir al admin marcarla como destacada tras aprobarla.
    private Boolean sugerirDestacado = false;
}
