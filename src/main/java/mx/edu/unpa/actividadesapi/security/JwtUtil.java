package mx.edu.unpa.actividadesapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mx.edu.unpa.actividadesapi.enums.TipoUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * US-00: Utilidad para generar y validar tokens JWT.
 *
 * Claims incluidos:
 *   sub   → id del usuario/alumno (como String)
 *   tipo  → TipoUsuario (ADMIN | PROFESOR | ALUMNO)
 *   role  → "ROLE_ADMIN" | "ROLE_PROFESOR" | "ROLE_ALUMNO"
 *   exp   → fecha de expiración
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiracion-ms:86400000}")
    private long expiracionMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Genera un token JWT firmado para el actor autenticado. */
    public String generar(Integer id, String nombre, TipoUsuario tipo) {
        String role = "ROLE_" + tipo.name();

        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("tipo", tipo.name())
                .claim("role", role)
                .claim("nombre", nombre)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracionMs))
                .signWith(key())
                .compact();
    }

    /** Extrae todos los claims de un token. */
    public Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Valida firma y expiración. Devuelve false si es inválido o expiró. */
    public boolean esValido(String token) {
        try {
            parsear(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public Integer getId(String token)       { return Integer.valueOf(parsear(token).getSubject()); }
    public TipoUsuario getTipo(String token) { return TipoUsuario.valueOf(parsear(token).get("tipo", String.class)); }
    public String getRole(String token)      { return parsear(token).get("role", String.class); }
}