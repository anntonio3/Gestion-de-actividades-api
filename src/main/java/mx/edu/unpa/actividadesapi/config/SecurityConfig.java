package mx.edu.unpa.actividadesapi.config;

import jakarta.servlet.DispatcherType;
import mx.edu.unpa.actividadesapi.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * US-00: Configuración de Spring Security.
 *
 * Reglas de acceso:
 *   Públicas (sin token):
 *     - /api/auth/**          → login, registro, recuperación
 *     - GET /api/calendario/** → calendario público
 *     - GET /api/corcho/**     → corcho digital público
 *     - /api/inscripciones/externo/** → inscripción de visitantes
 *     - GET /api/inscripciones/lote, /{id}/total, /{id}/estado  → conteo público
 *     - GET /api/**            → catálogos, recursos estáticos
 *
 *   Protegidas:
 *     - /api/admin/**         → solo ADMIN
 *     - /api/vicerrectoria/** → solo ADMIN
 *     - /api/actividades POST → PROFESOR o ADMIN
 *     - /api/actividades PUT  → PROFESOR o ADMIN
 *     - /api/inscripciones POST/DELETE → ALUMNO o PROFESOR
 *     - Cualquier otra        → autenticado (cualquier rol)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sin estado — usamos JWT, no sesiones HTTP
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Deshabilitar CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // US-00: habilitar CORS — delega la configuración al WebConfig
                .cors(cors -> {})

                // Deshabilitar el formulario de login de Spring Security
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Manejadores de error 401 y 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"No autenticado. Token ausente, inválido o expirado.\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"Acceso denegado. No tienes permiso para esta acción.\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                        // US-07: permitir dispatch asíncrono del SSE sin re-validar seguridad
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()

                        // ── Públicos sin token ────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()

                        // Calendario público completo
                        .requestMatchers(HttpMethod.GET, "/api/calendario/**").permitAll()

                        // Corcho digital
                        .requestMatchers(HttpMethod.GET, "/api/corcho/**").permitAll()

                        // Asistencia — el calendario público la usa sin login
                        .requestMatchers(HttpMethod.GET,  "/api/asistencia").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/asistencia/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/asistencia/**").permitAll()

                        // Inscripciones de visitantes externos (sin cuenta)
                        .requestMatchers("/api/inscripciones/externo/**").permitAll()

                        // Conteos públicos de inscripciones
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/lote").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/*/total").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/*/estado").permitAll()

                        // Catálogos (tipos, categorías, campus, carreras, etc.)
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tipos-actividad").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/campus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/carreras").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/departamentos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recursos/espacios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recursos/mobiliario").permitAll()

                        // Mapa y espacios públicos
                        .requestMatchers(HttpMethod.GET, "/api/admin/mapa/puntos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/espacios/**").permitAll()

                        // Archivos estáticos (imágenes subidas)
                        .requestMatchers("/uploads/**").permitAll()

                        // ── Solo ADMIN ────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/vicerrectoria/**").hasRole("ADMIN")

                        // ── PROFESOR o ADMIN ──────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/actividades").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/actividades/**").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/actividades/**").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers("/api/actividades/mis-solicitudes").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers("/api/actividades/*/recordatorio").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers("/api/actividades/*/inscritos/**").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers("/api/avisos/**").hasAnyRole("PROFESOR", "ADMIN")
                        .requestMatchers("/api/asistencia/**").hasAnyRole("PROFESOR", "ADMIN")

                        // ── Alumno, Profesor o Admin (logueado) ──────────────
                        .requestMatchers(HttpMethod.POST,   "/api/inscripciones/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/**").authenticated()
                        .requestMatchers("/api/inscripciones/mis-inscripciones").authenticated()

                        // ── Todo lo demás: autenticado ────────────────────────
                        .anyRequest().authenticated()
                )

                // Insertar el filtro JWT antes del filtro estándar de usuario/contraseña
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}