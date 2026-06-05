package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Petición para crear o actualizar un espacio físico.
 *
 * Restricción de ubicación (validada también en el service):
 *   - Espacio interno: idPunto presente, latitud/longitud/urlMaps nulos.
 *   - Espacio externo: latitud, longitud y urlMaps presentes, idPunto nulo.
 *   - No puede tener ambas cosas al mismo tiempo.
 *   - Debe tener al menos una de las dos.
 */
@Data
public class EspacioRequest {

    // Ubicación interna (mapa UNPA) — excluyente con los campos externos

    /** Nulo si el espacio es externo a la institución. */
    private Integer idPunto;

    // Ubicación externa (Google Maps) — excluyente con idPunto

    @DecimalMin(value = "-90.0",  message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0",   message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0",  message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;

    @Size(max = 1000, message = "La URL de Google Maps no debe superar 1000 caracteres")
    private String urlMaps;

    // Datos comunes del espacio

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String descripcion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidad;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no debe exceder 150 caracteres")
    private String ubicacion;

    /** Lista de equipamiento opcional. Puede venir vacía. */
    @Valid
    private List<EquipamientoRequest> equipamiento = new ArrayList<>();
}
