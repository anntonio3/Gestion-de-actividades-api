package mx.edu.unpa.actividadesapi.service.impl;

import lombok.RequiredArgsConstructor;
import mx.edu.unpa.actividadesapi.dto.request.*;
import mx.edu.unpa.actividadesapi.dto.response.LoginResponse;
import mx.edu.unpa.actividadesapi.dto.response.RecuperarContrasenaResponse;
import mx.edu.unpa.actividadesapi.enums.TipoUsuario;
import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.exception.ResourceNotFoundException;
import mx.edu.unpa.actividadesapi.model.Alumno;
import mx.edu.unpa.actividadesapi.model.TokenRecuperacion;
import mx.edu.unpa.actividadesapi.model.Usuario;
import mx.edu.unpa.actividadesapi.repository.AlumnoRepository;
import mx.edu.unpa.actividadesapi.repository.TokenRecuperacionRepository;
import mx.edu.unpa.actividadesapi.repository.UsuarioRepository;
import mx.edu.unpa.actividadesapi.service.AuthService;
import mx.edu.unpa.actividadesapi.service.correo.CorreoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository  alumnoRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final CorreoService correoService;

    // BCrypt reutilizable — sin Spring Security, instanciamos directamente
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    // Duración del token de recuperación
    private static final int HORAS_EXPIRACION_TOKEN = 1;

    // ====================================================================
    //  Login unificado
    // ====================================================================
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String id = request.getIdentificador().trim();
        log.info("Intento de login para identificador={}", id);

        // Determinar si es matricula (sin @) o correo (con @)
        boolean esCorreo = id.contains("@");

        if (esCorreo) {
            return loginConCorreo(id, request.getContrasena());
        } else {
            return loginConMatricula(id, request.getContrasena());
        }
    }

    private LoginResponse loginConCorreo(String correo, String contrasena) {
        // Buscar en usuarios (profesor / admin)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo.toLowerCase());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                log.warn("Login rechazado: usuario inactivo correo={}", correo);
                throw new BusinessException("Tu cuenta está desactivada. Contacta al administrador.");
            }
            if (!encoder.matches(contrasena, usuario.getContrasenaHash())) {
                log.warn("Login fallido: contraseña incorrecta correo={}", correo);
                throw new BusinessException("Correo o contraseña incorrectos.");
            }
            log.info("Login exitoso: usuario id={} rol={}", usuario.getIdUsuario(), usuario.getRol());
            return toLoginResponse(usuario);
        }

        // Buscar en alumnos (por si registraron correo institucional)
        Optional<Alumno> alumnoOpt = alumnoRepository.findByCorreo(correo.toLowerCase());
        if (alumnoOpt.isPresent()) {
            return validarYResponderAlumno(alumnoOpt.get(), contrasena);
        }

        log.warn("Login fallido: correo no encontrado correo={}", correo);
        throw new BusinessException("Correo o contraseña incorrectos.");
    }

    private LoginResponse loginConMatricula(String matricula, String contrasena) {
        Alumno alumno = alumnoRepository.findByMatricula(matricula)
                .orElseThrow(() -> {
                    log.warn("Login fallido: matrícula no encontrada matricula={}", matricula);
                    return new BusinessException("Matrícula o contraseña incorrectos.");
                });
        return validarYResponderAlumno(alumno, contrasena);
    }

    private LoginResponse validarYResponderAlumno(Alumno alumno, String contrasena) {
        if (!Boolean.TRUE.equals(alumno.getActivo())) {
            log.warn("Login rechazado: alumno inactivo matricula={}", alumno.getMatricula());
            throw new BusinessException("Tu cuenta está desactivada. Contacta al administrador.");
        }
        if (!encoder.matches(contrasena, alumno.getContrasenaHash())) {
            log.warn("Login fallido: contraseña incorrecta matricula={}", alumno.getMatricula());
            throw new BusinessException("Matrícula o contraseña incorrectos.");
        }
        log.info("Login exitoso: alumno id={}", alumno.getIdAlumno());
        return toLoginResponse(alumno);
    }

    // ====================================================================
    //  Registro de alumnos
    // ====================================================================
    @Override
    @Transactional
    public LoginResponse registrarAlumno(RegistroAlumnoRequest request) {
        log.info("Registro de alumno matricula={}", request.getMatricula());

        // Validar que las contraseñas coincidan
        if (!request.getContrasena().equals(request.getConfirmarContrasena())) {
            throw new BusinessException("Las contraseñas no coinciden.");
        }

        String matricula = request.getMatricula().trim();
        String correo    = request.getCorreo().trim().toLowerCase();

        // Validar unicidad de matrícula
        if (alumnoRepository.existsByMatricula(matricula)) {
            log.warn("Registro rechazado: matrícula duplicada matricula={}", matricula);
            throw new BusinessException("Ya existe una cuenta con esa matrícula.");
        }

        // Validar unicidad de correo (en alumnos y en usuarios)
        if (alumnoRepository.existsByCorreo(correo) || usuarioRepository.existsByCorreo(correo)) {
            log.warn("Registro rechazado: correo duplicado correo={}", correo);
            throw new BusinessException("Ya existe una cuenta con ese correo.");
        }

        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula);
        alumno.setNombre(request.getNombre().trim());
        alumno.setApellidos(request.getApellidos().trim());
        alumno.setCorreo(correo);
        alumno.setContrasenaHash(encoder.encode(request.getContrasena()));
        alumno.setActivo(true);

        Alumno guardado = alumnoRepository.save(alumno);
        log.info("Alumno registrado id={} matricula={}", guardado.getIdAlumno(), guardado.getMatricula());

        return toLoginResponse(guardado);
    }

    // ====================================================================
    //  Recuperación de contraseña
    // ====================================================================
    @Override
    @Transactional
    public RecuperarContrasenaResponse solicitarRecuperacion(RecuperarContrasenaRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();
        log.info("Solicitud de recuperación de contraseña para correo={}", correo);

        // Buscar en usuarios (profesor/admin)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            tokenRepository.invalidarTokensDeUsuario(usuario.getIdUsuario());
            String tokenStr = generarYGuardarTokenUsuario(usuario);
            String url = construirUrlRecuperacion(tokenStr);
            return correoService.enviarRecuperacion(correo, url, tokenStr);
        }

        // Buscar en alumnos
        Optional<Alumno> alumnoOpt = alumnoRepository.findByCorreo(correo);
        if (alumnoOpt.isPresent()) {
            Alumno alumno = alumnoOpt.get();
            tokenRepository.invalidarTokensDeAlumno(alumno.getIdAlumno());
            String tokenStr = generarYGuardarTokenAlumno(alumno);
            String url = construirUrlRecuperacion(tokenStr);
            return correoService.enviarRecuperacion(correo, url, tokenStr);
        }

        // Respuesta genérica: no revelamos si el correo existe o no (seguridad)
        log.warn("Recuperación solicitada para correo no registrado={}", correo);
        RecuperarContrasenaResponse respuesta = new RecuperarContrasenaResponse();
        respuesta.setMensaje("Si el correo está registrado, recibirás un enlace en breve.");
        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public void verificarToken(String token) {
        TokenRecuperacion tr = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Verificación de token inválido token={}", token);
                    return new BusinessException("El enlace de recuperación no es válido o ya fue utilizado.");
                });

        if (!tr.esValido()) {
            log.warn("Token expirado o ya usado token={}", token);
            throw new BusinessException("El enlace de recuperación expiró o ya fue utilizado.");
        }
    }

    @Override
    @Transactional
    public void restablecerContrasena(RestablecerContrasenaRequest request) {
        log.info("Intento de restablecimiento de contraseña");

        if (!request.getNuevaContrasena().equals(request.getConfirmarContrasena())) {
            throw new BusinessException("Las contraseñas no coinciden.");
        }

        TokenRecuperacion tr = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BusinessException("El enlace de recuperación no es válido."));

        if (!tr.esValido()) {
            throw new BusinessException("El enlace de recuperación expiró o ya fue utilizado.");
        }

        String nuevoHash = encoder.encode(request.getNuevaContrasena());

        if (tr.getUsuario() != null) {
            tr.getUsuario().setContrasenaHash(nuevoHash);
            usuarioRepository.save(tr.getUsuario());
            log.info("Contraseña restablecida para usuario id={}", tr.getUsuario().getIdUsuario());
        } else if (tr.getAlumno() != null) {
            tr.getAlumno().setContrasenaHash(nuevoHash);
            alumnoRepository.save(tr.getAlumno());
            log.info("Contraseña restablecida para alumno id={}", tr.getAlumno().getIdAlumno());
        }

        // Marcar como usado para que no se pueda reutilizar
        tr.setUsado(true);
        tokenRepository.save(tr);
    }

    // ====================================================================
    //  Helpers privados
    // ====================================================================

    private String generarYGuardarTokenUsuario(Usuario usuario) {
        TokenRecuperacion tr = new TokenRecuperacion();
        tr.setToken(UUID.randomUUID().toString());
        tr.setUsuario(usuario);
        tr.setFechaExpiracion(LocalDateTime.now().plusHours(HORAS_EXPIRACION_TOKEN));
        tokenRepository.save(tr);
        return tr.getToken();
    }

    private String generarYGuardarTokenAlumno(Alumno alumno) {
        TokenRecuperacion tr = new TokenRecuperacion();
        tr.setToken(UUID.randomUUID().toString());
        tr.setAlumno(alumno);
        tr.setFechaExpiracion(LocalDateTime.now().plusHours(HORAS_EXPIRACION_TOKEN));
        tokenRepository.save(tr);
        return tr.getToken();
    }

    private String construirUrlRecuperacion(String token) {
        // En producción esta URL viene de application.properties
        return "http://localhost:4200/auth/restablecer/" + token;
    }

    private LoginResponse toLoginResponse(Usuario u) {
        LoginResponse dto = new LoginResponse();
        dto.setId(u.getIdUsuario());
        dto.setNombre(u.getNombre());
        dto.setApellidos(u.getApellidos());
        dto.setCorreo(u.getCorreo());
        dto.setMatricula(null);
        dto.setIniciales(calcularIniciales(u.getNombre(), u.getApellidos()));
        // Convertimos el enum Rol a TipoUsuario
        dto.setTipoUsuario(TipoUsuario.valueOf(u.getRol().name()));
        return dto;
    }

    private LoginResponse toLoginResponse(Alumno a) {
        LoginResponse dto = new LoginResponse();
        dto.setId(a.getIdAlumno());
        dto.setNombre(a.getNombre());
        dto.setApellidos(a.getApellidos());
        dto.setCorreo(a.getCorreo());
        dto.setMatricula(a.getMatricula());
        dto.setIniciales(calcularIniciales(a.getNombre(), a.getApellidos()));
        dto.setTipoUsuario(TipoUsuario.ALUMNO);
        return dto;
    }

    private String calcularIniciales(String nombre, String apellidos) {
        String ini = "";
        if (nombre    != null && !nombre.isBlank())    ini += nombre.trim().charAt(0);
        if (apellidos != null && !apellidos.isBlank()) ini += apellidos.trim().charAt(0);
        return ini.toUpperCase();
    }
}
