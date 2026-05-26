package mx.edu.unpa.actividadesapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.TipoUsuario;

/**
 * Respuesta del POST /api/auth/login.
 * El frontend guarda este objeto en el servicio de sesión.
 * Sin JWT por ahora; se agrega en el sprint de seguridad real.
 */
@Data
public class LoginResponse {

    private Integer id;          // id_usuario o id_alumno según el tipo
    private String  nombre;
    private String  apellidos;
    private String  correo;
    private String  matricula;   // solo para alumnos, null para los demás
    private String  iniciales;
    @JsonProperty("tipo")
    private TipoUsuario tipoUsuario;
}
