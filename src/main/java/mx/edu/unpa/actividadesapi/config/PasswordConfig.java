package mx.edu.unpa.actividadesapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configura el encoder de contrasenas.
 * Se usa BCrypt con strength 10 (valor por defecto, buen equilibrio
 * entre seguridad y rendimiento en sistemas universitarios de bajo trafico).
 */
@Configuration
public class PasswordConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
