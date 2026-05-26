package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Inscripcion formal de un usuario o alumno a una actividad.
 * Solo aplica cuando actividad.requiereInscripcion = true.
 * Un actor (usuario o alumno) no puede inscribirse dos veces
 * a la misma actividad ni a dos actividades en el mismo horario.
 */
@Data
@Entity
@Table(name = "inscripciones_actividad")
public class InscripcionActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion")
    private Integer idInscripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    // Usuario institucional (profesor / admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;

    // Alumno
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = true)
    private Alumno alumno;

    @Column(name = "fecha_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fechaInscripcion;

    @PrePersist
    protected void onCreate() {
        this.fechaInscripcion = LocalDateTime.now();
    }
}
