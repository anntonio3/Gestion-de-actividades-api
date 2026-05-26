package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Token de un solo uso para recuperación de contraseña.
 * Un registro tiene id_usuario O id_alumno, nunca ambos.
 * Expira una hora después de crearse.
 */
@Data
@Entity
@Table(name = "tokens_recuperacion")
public class TokenRecuperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer idToken;

    @Column(name = "token", nullable = false, columnDefinition = "CHAR(36)", unique = true)
    private String token;

    // Relación con usuarios (profesor / admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    // Relación con alumnos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = true)
    private Alumno alumno;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Indica si el token sigue siendo válido
    public boolean esValido() {
        return !Boolean.TRUE.equals(this.usado)
                && LocalDateTime.now().isBefore(this.fechaExpiracion);
    }
}
