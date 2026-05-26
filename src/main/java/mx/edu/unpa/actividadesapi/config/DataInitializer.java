package mx.edu.unpa.actividadesapi.config;

import mx.edu.unpa.actividadesapi.repository.AlumnoRepository;
import mx.edu.unpa.actividadesapi.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Migra las contrasenas invalidas del seed al arrancar el backend.
 * Aplica tanto a usuarios institucionales (profesores/admin) como a alumnos.
 *
 * Regla de contrasena inicial:
 *   - Usuarios: parte antes del @ del correo  (ej. admin@unpa.edu.mx -> "admin")
 *   - Alumnos:  matricula                     (ej. matricula 20231001 -> "20231001")
 *
 * Es inocuo: si el hash ya es BCrypt valido ($2a$...) lo deja intacto.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        migrarUsuarios();
        migrarAlumnos();
    }

    // ── Usuarios institucionales (profesores y admins) ─────────────

    private void migrarUsuarios() {
        log.info("Verificando contrasenas de usuarios institucionales...");
        int migrados = 0;

        for (var usuario : usuarioRepository.findAll()) {
            if (necesitaMigracion(usuario.getContrasenaHash())) {
                String correo = usuario.getCorreo();
                // Contrasena inicial = parte antes del @
                String contrasenaInicial = correo.substring(0, correo.indexOf('@'));
                usuario.setContrasenaHash(passwordEncoder.encode(contrasenaInicial));
                usuarioRepository.save(usuario);
                log.info("  Usuario migrado: correo='{}' -> contrasena inicial='{}'",
                        correo, contrasenaInicial);
                migrados++;
            }
        }

        if (migrados == 0) {
            log.info("  Todos los usuarios ya tienen hash BCrypt valido.");
        } else {
            log.info("  {} usuario(s) migrado(s).", migrados);
        }
    }

    // ── Alumnos ────────────────────────────────────────────────────

    private void migrarAlumnos() {
        log.info("Verificando contrasenas de alumnos...");
        int migrados = 0;

        for (var alumno : alumnoRepository.findAll()) {
            if (necesitaMigracion(alumno.getContrasenaHash())) {
                // Contrasena inicial = matricula del alumno
                String contrasenaInicial = alumno.getMatricula();
                alumno.setContrasenaHash(passwordEncoder.encode(contrasenaInicial));
                alumnoRepository.save(alumno);
                log.info("  Alumno migrado: matricula='{}' -> contrasena inicial='{}'",
                        alumno.getMatricula(), contrasenaInicial);
                migrados++;
            }
        }

        if (migrados == 0) {
            log.info("  Todos los alumnos ya tienen hash BCrypt valido.");
        } else {
            log.info("  {} alumno(s) migrado(s).", migrados);
        }
    }

    // ── Helper ─────────────────────────────────────────────────────

    /**
     * Devuelve true si el hash es nulo, vacio o no es un hash BCrypt valido.
     * Los hashes BCrypt reales siempre empiezan con "$2a$", "$2b$" o "$2y$".
     */
    private boolean necesitaMigracion(String hash) {
        return hash == null || hash.isBlank() || !hash.startsWith("$2");
    }
}