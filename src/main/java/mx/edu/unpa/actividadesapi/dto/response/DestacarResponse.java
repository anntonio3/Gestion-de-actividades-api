package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * US-26: Confirmacion tras marcar una actividad como destacada.
 */
@Data
public class DestacarResponse {

    private Integer idActividad;
    private String nombre;
    private Boolean destacadoActivo;
    private String nombreAdmin;
    private LocalDateTime fechaDestacado;
}
