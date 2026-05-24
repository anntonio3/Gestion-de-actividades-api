package mx.edu.unpa.actividadesapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.Rol;

@Data
public class UsuarioEditarRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@unpa\\.edu\\.mx$",
            message = "El correo debe pertenecer al dominio @unpa.edu.mx"
    )
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String correo;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    // La contraseña no se edita desde este flujo.
    // Cuando se implemente US-00, habrá un endpoint dedicado para resetear contraseña.
}
