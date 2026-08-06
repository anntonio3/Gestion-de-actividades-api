package mx.edu.unpa.actividadesapi.service;

import mx.edu.unpa.actividadesapi.dto.request.*;
import mx.edu.unpa.actividadesapi.dto.response.LoginResponse;
import mx.edu.unpa.actividadesapi.dto.response.RecuperarContrasenaResponse;

public interface AuthService {

    // Login unificado: acepta matricula (alumno) o correo (profesor/admin)
    LoginResponse login(LoginRequest request);

    // Registro solo para alumnos
    LoginResponse registrarAlumno(RegistroAlumnoRequest request);

    // Solicitar token de recuperacion por correo
    RecuperarContrasenaResponse solicitarRecuperacion(RecuperarContrasenaRequest request);

    // Validar que el token sigue vigente antes de mostrar el formulario
    void verificarToken(String token);

    // Cambiar contraseña usando el token recibido por correo
    void restablecerContrasena(RestablecerContrasenaRequest request);
    // logout real con blacklist
    void logout(String authorizationHeader);
}
