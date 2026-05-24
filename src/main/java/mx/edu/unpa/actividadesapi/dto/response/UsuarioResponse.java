package mx.edu.unpa.actividadesapi.dto.response;

import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.Rol;

import java.time.LocalDateTime;

@Data
public class UsuarioResponse {

    private Integer idUsuario;
    private String nombre;
    private String apellidos;
    private String correo;
    private Rol rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;

    // Iniciales calculadas para el avatar en el frontend (ej. "CHL" para Sandro Hernández López)
    private String iniciales;
}
