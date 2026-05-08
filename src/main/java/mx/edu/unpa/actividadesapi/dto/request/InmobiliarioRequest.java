package mx.edu.unpa.actividadesapi.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InmobiliarioRequest {

    // ── Campos de la tabla base: recursos ───────────────────────────
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    // ── Campos propios de recursos_mobiliario (US-14) ───────────────

    private String codigo;           // opcional

    private String numInventario;    // opcional

    @NotNull(message = "Las existencias son obligatorias")
    @Min(value = 0, message = "Las existencias no pueden ser negativas")
    private Integer existencias;

    @NotNull(message = "El número disponible es obligatorio")
    @Min(value = 0, message = "Los disponibles no pueden ser negativos")
    private Integer disponibles;

    private String nota;

    // La foto llega como MultipartFile en el controller (no en este DTO)
}
