package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;
import mx.edu.unpa.actividadesapi.enums.Sexo;

import java.time.LocalDateTime;

/**
 * US-24: Inscripcion de una persona externa a la institucion
 * en una actividad publica que requiere inscripcion.
 *
 * El campo id_visitante reutiliza el mecanismo de cookie HTTP-only
 * de US-12 (tabla visitantes) para evitar que la misma persona
 * se inscriba varias veces al mismo evento desde el mismo navegador.
 */
@Data
@Entity
@Table(name = "inscripciones_externo")
public class InscripcionExterno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion_externo")
    private Integer idInscripcionExterno;

    // Actividad en la que se inscribe el externo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    // Visitante anonimo identificado por cookie HTTP-only (anti-duplicado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_visitante", nullable = true)
    private Visitante visitante;

    // Datos personales del externo
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    // Ciudad, estado o institucion de donde proviene
    @Column(name = "procedencia", nullable = false, length = 150)
    private String procedencia;

    // Correo electronico opcional para contacto posterior
    @Column(name = "correo", length = 150)
    private String correo;

    // Telefono opcional para contacto posterior
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "fecha_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fechaInscripcion;

    @PrePersist
    protected void onCreate() {
        this.fechaInscripcion = LocalDateTime.now();
    }
}