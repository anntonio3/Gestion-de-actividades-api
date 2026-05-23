package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * US-17/US-19: Datos para crear o editar un aviso del corcho.
 * La foto viaja aparte como MultipartFile en el controller.
 */
@Data
public class AvisoRequest {

    // En US-17 viene del frontend; en US-19 igual (mientras no haya auth)
    @NotNull(message = "El id del profesor es obligatorio")
    private Integer idProfesor;

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 150, message = "El titulo no puede superar 150 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 5000, message = "La descripcion es demasiado larga")
    private String descripcion;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDate fechaEvento;

    // Hora opcional segun decision de diseno
    private LocalTime horaEvento;
}
