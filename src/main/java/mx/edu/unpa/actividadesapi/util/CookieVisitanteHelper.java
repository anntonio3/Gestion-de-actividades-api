package mx.edu.unpa.actividadesapi.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

/**
 * Maneja la cookie del visitante anonimo.
 * - Si la peticion la trae, devuelve su valor.
 * - Si no, genera UUID nuevo y lo escribe en la respuesta.
 *
 * Notas:
 * - HttpOnly = true para que JS no la pueda leer (proteccion XSS).
 * - SameSite = Lax para que viaje con navegacion normal pero no en CSRF.
 * - 1 ano de duracion; al alumno no le interesa que expire pronto.
 */
public final class CookieVisitanteHelper {

    public static final String COOKIE_NAME = "visitante_id";
    private static final int MAX_AGE_SEGUNDOS = 60 * 60 * 24 * 365; // 1 ano

    private CookieVisitanteHelper() {}

    public static String obtenerOCrear(HttpServletRequest req, HttpServletResponse res) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (COOKIE_NAME.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        // No existe: generar y enviar
        String nuevo = UUID.randomUUID().toString();
        Cookie cookie = new Cookie(COOKIE_NAME, nuevo);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SEGUNDOS);
        // setSameSite no esta en Cookie; se agrega via header manual
        res.addCookie(cookie);
        res.addHeader("Set-Cookie",
                COOKIE_NAME + "=" + nuevo +
                        "; Max-Age=" + MAX_AGE_SEGUNDOS +
                        "; Path=/; HttpOnly; SameSite=Lax");
        return nuevo;
    }
}