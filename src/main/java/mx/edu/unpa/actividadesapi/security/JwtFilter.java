package mx.edu.unpa.actividadesapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.unpa.actividadesapi.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * US-00: Filtro JWT.
 * Extrae el token del header Authorization: Bearer <token>,
 * valida firma + expiración + blacklist y pone la autenticación
 * en el SecurityContext para que Spring Security proteja los endpoints.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository blacklistRepository;

    public JwtFilter(JwtUtil jwtUtil, TokenBlacklistRepository blacklistRepository) {
        this.jwtUtil = jwtUtil;
        this.blacklistRepository = blacklistRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // Validar firma y expiración
        if (!jwtUtil.esValido(token)) {
            log.debug("Token inválido o expirado en {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        // Validar que no esté en la blacklist (logout previo)
        if (blacklistRepository.existsByToken(token)) {
            log.debug("Token en blacklist para {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        // Autenticar en el SecurityContext
        String role = jwtUtil.getRole(token);
        var auth = new UsernamePasswordAuthenticationToken(
                jwtUtil.getId(token),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}