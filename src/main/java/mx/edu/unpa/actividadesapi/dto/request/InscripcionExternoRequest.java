package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.Sexo;

/**
 * US-24: Datos que proporciona una persona externa para inscribirse
 * en una actividad publica con inscripcion requerida.
 */
@Data
public class InscripcionExternoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 1, message = "La edad debe ser mayor a 0")
    @Max(value = 120, message = "La edad no puede superar 120 anios")
    private Integer edad;

    @NotNull(message = "El sexo es obligatorio")
    private Sexo sexo;

    @NotBlank(message = "La procedencia es obligatoria")
    @Size(max = 150, message = "La procedencia no puede superar 150 caracteres")
    private String procedencia;

    // Correo opcional; si viene, se valida formato y se usa para evitar duplicados
    @Email(message = "El formato del correo no es valido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String correo;

    // Telefono opcional para contacto posterior
    @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
    private String telefono;
}

