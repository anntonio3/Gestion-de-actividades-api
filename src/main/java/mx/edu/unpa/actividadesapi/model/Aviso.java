package mx.edu.unpa.actividadesapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * US-17/US-18/US-19/US-20: Aviso del corcho digital.
 * El profesor lo crea y se publica automaticamente, sin pasar
 * por revision del vicerrector (a diferencia de Actividad).
 */
@Data
@Entity
@Table(name = "avisos")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aviso")
    private Integer idAviso;

    // Profesor que publica el aviso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesor", nullable = false)
    private Usuario profesor;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Fecha del evento que se anuncia (informativa, no expira el aviso)
    @Column(name = "fecha_evento", nullable = false)
    private LocalDate fechaEvento;

    // Hora opcional: a veces solo importa el dia
    @Column(name = "hora_evento")
    private LocalTime horaEvento;

    // URL completa servida por /uploads/**
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        this.fechaPublicacion = ahora;
        this.fechaActualizacion = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
