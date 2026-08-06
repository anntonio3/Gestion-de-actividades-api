package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * US-00: Tokens invalidados al hacer logout.
 * El JwtFilter consulta esta tabla antes de aceptar un token.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardamos el token completo para comparación exacta
    @Column(name = "token", nullable = false, length = 512, unique = true)
    private String token;

    @Column(name = "fecha_invalidacion", nullable = false)
    private LocalDateTime fechaInvalidacion;

    // Fecha en que el token hubiera expirado — usada para limpieza periódica
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @PrePersist
    protected void onCreate() {
        this.fechaInvalidacion = LocalDateTime.now();
    }
}